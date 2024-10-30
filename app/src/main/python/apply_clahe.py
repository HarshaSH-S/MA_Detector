import numpy as np
import cv2
from os.path import dirname, join
import tflite_runtime.interpreter as tflite

def clahe_image(image_bytes, dimension, threshold):
    # convert the byte array into a numpy array
    nparr = np.frombuffer(image_bytes, np.uint8)
    image1 = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

    image = image1[:,:,1]

    clahe = cv2.createCLAHE(clipLimit = 8, tileGridSize=(8,8))
    image = clahe.apply(image)

    model_path = join(dirname(__file__), "seg_model.tflite")

    # Load TFLite model and allocate tensors.
    interpreter = tflite.Interpreter(model_path = model_path)
    interpreter.allocate_tensors()

    # Get input and output tensors.
    input_details = interpreter.get_input_details()
    output_details = interpreter.get_output_details()

    # threshold = 0.50 # increase this threshold if more false positives are coming
    [m,n] = image.shape
    step = 12

    i_start = 0
    j_start = 0
    m_new = m
    n_new = n
    if(m > dimension):
        i_start = ((m - dimension) // 2)
        m_new = i_start + dimension - 1

    if(n > dimension):
        j_start = ((n - dimension) // 2)
        n_new = j_start + dimension - 1

#     if(m_new > n_new):
#         m_new = n_new
#     else:
#         n_new = m_new

    final_img = np.zeros((m_new - i_start + 1, n_new - j_start + 1))

    for i in range(i_start,m_new,step):
        for j in range(j_start,n_new,step):
            if((i+48)>(m_new-1) or (j+48)>(n_new-1)):
                pass
            else:
                # getting image patches
                patch_img = image[i:i+48,j:j+48]
                patch_img = np.expand_dims(patch_img, axis=-1)
                patch_img = np.expand_dims(patch_img, axis=0)

                interpreter.set_tensor(input_details[0]['index'], patch_img.astype(np.float32))
                interpreter.set_tensor(input_details[1]['index'], patch_img.astype(np.float32))
                interpreter.invoke()
                inter_img = interpreter.get_tensor(output_details[0]['index'])

                inter_img = (inter_img > threshold)
                final_img[(i - i_start):((i - i_start)+48),(j - j_start):((j - j_start)+48)] = final_img[(i - i_start):(i - i_start)+48,(j - j_start):(j - j_start)+48] + np.squeeze(inter_img)

    psm_th2 = final_img / np.max(final_img)
    psm_th2 = (psm_th2 > 0.05).astype(float)

    psm_th2 = psm_th2 * 255

    for i in (i_start,m_new-1):
        for j in range(j_start,n_new-1):
            image1[i,j,0] = 0
            image1[i,j,1] = 255
            image1[i,j,2] = 0

    for j in (j_start,n_new-1):
        for i in range(i_start,m_new-1):
            image1[i,j,0] = 0
            image1[i,j,1] = 255
            image1[i,j,2] = 0

    for i in range(i_start,m_new):
        for j in range(j_start,n_new):
            if(psm_th2[i-i_start, j-j_start] == 255):
                image1[i,j,0] = 255
                image1[i,j,1] = 255
                image1[i,j,2] = 0

    _, encoded_image = cv2.imencode('.png', image1)
    return encoded_image.tobytes()
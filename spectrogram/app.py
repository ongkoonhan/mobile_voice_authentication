import time
import copy
import random
import json

import numpy as np
import os
import io
import matplotlib.pyplot as plt
import librosa
import librosa.display
from scipy.io import wavfile
from flask import Flask, request, jsonify, render_template, send_file
import soundfile as sf

from firebase import firebase

firebase = firebase.FirebaseApplication('https://something-f0057.firebaseio.com/', None)







# import pickle

def get_sliding_img_slice_from_spectrogram(spectrogram, depth=3, sliding_ratio=2):
### Combine multiple sliding greyscale img slices into an n-depth image
    height = spectrogram.shape[0]
    slide_step = height//sliding_ratio
    img_slice = np.zeros((depth,height,height))   # initialize empty img (pytorch style)
    # Get random start idx
    slice_start = random.randint(0, spectrogram.shape[1] - (slide_step*(depth+1)) - 1)
    for i in range(depth):
        img_slice[i,:,:] = spectrogram[:, slice_start:slice_start+height]   # get slice (pytorch style)
        slice_start += slide_step   # slide
    img_slice = img_slice.astype("float32")
    return img_slice

app = Flask(__name__)
# model = pickle.load(open('model.pkl', 'rb'))


@app.route('/')
def home():
    if os.path.exists("audio.wav"):
        os.remove("audio.wav")
    if os.path.exists("fig.png"):
        os.remove("fig.png")
    return render_template('index.html')


@app.route('/spectrogram',methods=['POST'])
def spectrogram():
    '''
    generate spectrogram from wav file
    '''

    # data = request.get_data()
    # wav = "123"


    f = request.files["wav"]
    # f.save(secure_filename(f.filename))

    # with open('upload.wav', mode='bx') as f:
    #     f.write(data)

    # y, sr = sf.read(f)

    # sf.write('new_file.wav', data, samplerate)

    y, sr = librosa.load(f)

    S = librosa.feature.melspectrogram(y=y, sr=sr, n_mels=128,fmax=8000)
    arr = get_sliding_img_slice_from_spectrogram(S)
    lists = arr.tolist()
    json_str = json.dumps(lists)
    # plt.figure(figsize=(10, 4))
    # S_dB = librosa.power_to_db(S, ref=np.max)
    # librosa.display.specshow(S_dB, x_axis='time',y_axis='mel',sr=sr,fmax=8000)
    # plt.colorbar(format='%+2.0f dB')
    # plt.title('Mel-frequency spectrogram')
    # plt.tight_layout()
    # # plt.show()
    # output = plt.savefig("fig.png", dpi=150)


    result = firebase.post('/spectrogram', data={"0":json_str}, params={'print': 'pretty'})

    return render_template('index.html', prediction_text=result)
    # return send_file("fig.png", mimetype="image/png")

if __name__ == "__main__":
    app.run(debug=True)
import numpy as np
import librosa
import librosa.display
from flask import Flask, request, render_template
from VoiceAuthentication import VoiceAuthentication

application = Flask(__name__)


@application.route('/')
def home():
    return render_template('index.html')


@application.route('/verify', methods=['POST'])
def verify():
    '''
    verify from wav files
    '''
    f1 = request.files["wav1"]
    f2 = request.files["wav2"]

    result = VoiceAuthentication('mobile_model.pt').authenticate(f1, f2)

    return render_template('index.html', display=result)


if __name__ == "__main__":
    application.run(debug=True)

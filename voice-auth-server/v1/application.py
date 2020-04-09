import os
from flask import Flask, request, render_template, jsonify

from VoiceAuthentication import VoiceAuthentication
import app_setup


application = Flask(__name__)

VOICE_AUTH_MODEL = VoiceAuthentication('mobile_model.pt')


@application.route('/')
def home():
    return render_template('index.html')


@application.route('/verify', methods=['POST'])
def verify():
    '''
    verify from wav files
    '''
    def get_and_save_temp_file(request_file):
        file_storage = request.files[request_file]
        temp_path = os.path.join(app_setup.TEMP_FOLDER, file_storage.filename)
        file_storage.save(temp_path)
        return temp_path

    # f1 = request.files["wav1"]
    # f2 = request.files["wav2"]
    f1_path = get_and_save_temp_file("wav1")
    f2_path = get_and_save_temp_file("wav2")

    result = VOICE_AUTH_MODEL.authenticate(f1_path, f2_path)

    # return render_template('index.html', display=result[0])
    return jsonify(verification=result[0],
                   votes=result[1],
                   percentage="{}%".format(result[2]))


if __name__ == "__main__":
    application.run(debug=True)

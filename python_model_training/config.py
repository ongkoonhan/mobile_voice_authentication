import os


### Path config

data_folder = os.path.join(r"D:\Desktop\data\speech_audio\voxceleb1")
audio_dev_folder = os.path.join(data_folder, r"audio\vox1_dev_wav")
audio_test_folder = os.path.join(data_folder, r"audio\vox1_test_wav")

output_data_folder = os.path.join(r"D:\Desktop\projects\speaker_recognition_voxceleb1\output_data")
models_folder = os.path.join(output_data_folder, "models")


### Data processing config

n_mels = 128
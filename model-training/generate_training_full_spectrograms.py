import os
import json
import numpy as np
import librosa
from time import time

from config import audio_dev_folder, audio_test_folder, output_data_folder
from config import n_mels


n_mels = n_mels


def generate_spectrograms(input_file, output_folder, audio_folder):
    ### Create full spectrograms for each speaker
    with open(input_file, 'r', encoding='utf-8') as f:
        training_dataset = json.load(f)
    t0 = time()
    t1 = time()
    for speaker, data in training_dataset.items():
        speaker_folder = os.path.join(audio_folder, "wav", speaker)
        sample_files = data["files"]
        samples_combined = []
        # Concatenate all samples into one long sample
        for sample_file in sample_files:
            file = os.path.join(speaker_folder, sample_file)
            samples, sample_rate = librosa.core.load(file)
            samples_combined.append(samples)
        samples = np.concatenate(tuple(samples_combined))
        # Time
        print("{0} samples: {1:.4f} seconds".format(speaker, time() - t1))
        t1 = time()
        # Spectrogram
        S = librosa.feature.melspectrogram(samples, n_mels=n_mels)
        S_dB = librosa.power_to_db(S, ref=1.0)
        # Save as np array
        out_file = os.path.join(output_folder, str(speaker) + ".npy")
        np.save(out_file, S_dB)
        # Time
        print("{0} spectrogram: {1:.4f} seconds".format(speaker, time()-t1))
        t1 = time()
    # Total time
    print("Total time: {} seconds".format(time()-t0))



def main():
    # ### Training data
    # output_folder = os.path.join(output_data_folder, "training_dataset_full_spectrogram/vox1_dev_wav")  # Spectrogram folder
    # input_file = os.path.join(output_data_folder, "training_dataset_dev.json")  # JSON file for dataset
    # generate_spectrograms(input_file, output_folder, audio_dev_folder)

    ### Test data
    output_folder = os.path.join(output_data_folder, "training_dataset_full_spectrogram/vox1_test_wav")  # Spectrogram folder
    input_file = os.path.join(output_data_folder, "training_dataset_test.json")  # JSON file for dataset
    generate_spectrograms(input_file, output_folder, audio_test_folder)


if __name__ == "__main__":
    main()
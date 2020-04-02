import os
import pickle

from data_generators import ContrastiveDataGenerator, VerificationDataGenerator
from config import output_data_folder
from config import n_mels


IMG_HEIGHT = n_mels
validation_spectorgram_folder = os.path.join(output_data_folder, "training_dataset_full_spectrogram/vox1_test_wav")
validation_sets_folder = os.path.join(output_data_folder, "validation_sets")


def generate_contrastive_validation_set():
    spectrogram_samples_files = [os.path.join(validation_spectorgram_folder, file) for file in os.listdir(validation_spectorgram_folder)]
    candidate_size = 5
    batch_size = 15
    num_batches = 2000 // batch_size
    num_sub_samples = len(spectrogram_samples_files)
    validation_data_generator = ContrastiveDataGenerator(spectrogram_samples_files, candidate_size, batch_size, num_batches, num_sub_samples, IMG_HEIGHT)
    # create batches in list
    batches = []
    for i, batch in zip(range(num_batches), validation_data_generator.generate_batches()):
        batches.append(batch)
    print(len(batches))
    # save pickle
    out_file = os.path.join(validation_sets_folder, "contrastive_validation_set.pickle")
    with open(out_file, 'wb+') as f: pickle.dump(batches, f)


def generate_verification_validation_set():
    spectrogram_samples_files = [os.path.join(validation_spectorgram_folder, file) for file in os.listdir(validation_spectorgram_folder)]
    batch_size = 160
    num_batches = 2000 // batch_size
    num_sub_samples = len(spectrogram_samples_files)
    validation_data_generator = VerificationDataGenerator(spectrogram_samples_files, batch_size, num_batches, num_sub_samples, IMG_HEIGHT)
    # create batches in list
    batches = []
    for i, batch in zip(range(num_batches), validation_data_generator.generate_batches()):
        batches.append(batch)
    print(len(batches))
    # save pickle
    out_file = os.path.join(validation_sets_folder, "verification_validation_set.pickle")
    with open(out_file, 'wb+') as f: pickle.dump(batches, f)




def main():
    generate_contrastive_validation_set()
    generate_verification_validation_set()



if __name__ == "__main__":
    main()
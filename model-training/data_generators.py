import random
import torch
import numpy as np
from skimage.color import gray2rgb


class BaseDataGenerator:

    @classmethod
    def get_sliding_img_slice_from_spectrogram(cls, spectrogram, depth=3, sliding_ratio=2):
        ### Combine multiple sliding greyscale img slices into an n-depth image
        height = spectrogram.shape[0]
        slide_step = height // sliding_ratio
        img_slice = np.zeros((depth, height, height))  # initialize empty img (pytorch style)
        # Get random start idx
        slice_start = random.randint(0, spectrogram.shape[1] - (slide_step * (depth + 1)) - 1)
        for i in range(depth):
            img_slice[i, :, :] = spectrogram[:, slice_start:slice_start + height]  # get slice (pytorch style)
            slice_start += slide_step  # slide
        img_slice = img_slice.astype("float32")
        img_slice = img_slice / np.amax(np.absolute(img_slice))  # normalize to range [-1, 1]
        return img_slice

    @classmethod
    def spectrogram_to_RGB(cls, spectrogram):
        assert len(spectrogram.shape) == 2, "Spectrogram input should be a 2D array"
        spectrogram_rgb = gray2rgb(spectrogram)
        return spectrogram_rgb



class ContrastiveDataGenerator(BaseDataGenerator):

    def __init__(self, spectrogram_samples_files, candidate_size, batch_size, num_batches, num_sub_samples, img_height):
        self.spectrogram_samples_files = spectrogram_samples_files  # list of filepaths
        self.candidate_size = candidate_size  # 1 positive, n-1 negatives
        self.batch_size = batch_size  # batch size
        self.num_batches = num_batches  # num batches per epoch
        self.num_sub_samples = num_sub_samples  # num sub-samples per epoch
        self.img_height = img_height  # height of square img to be generated in the batches
        self.sub_samples = []  # list of RGB converted spectrograms

    def generate_batches(self):
        # while True:
        self.create_sub_samples()
        # batches per epoch
        for _ in range(self.num_batches):
            # create batch
            labels = []
            query_and_candidate_imgs = [[] for _ in range(self.candidate_size + 1)]
            for _ in range(self.batch_size):
                sample_spectrograms_indices = random.sample(range(self.num_sub_samples), self.candidate_size)  # sample candidates
                pos_idx = sample_spectrograms_indices[0]  # positive sample
                # Generate query image
                query_img = self.get_sliding_img_slice_from_spectrogram(self.sub_samples[pos_idx])
                # Generate batch images
                random.shuffle(sample_spectrograms_indices)
                candidate_imgs = [self.get_sliding_img_slice_from_spectrogram(self.sub_samples[idx]) for idx in sample_spectrograms_indices]
                # get class label / idx of positive sample
                pos_candidate_idx = sample_spectrograms_indices.index(pos_idx)
                labels.append(pos_candidate_idx)
                # Add to output list
                for i, img in enumerate([query_img, *candidate_imgs]): query_and_candidate_imgs[i].append(img)
            # Convert to tensor
            labels = torch.tensor(labels)
            input_imgs = torch.tensor(query_and_candidate_imgs)
            yield (input_imgs, labels)

    def create_sub_samples(self):
        self.sub_samples = []  # reset
        files = random.sample(self.spectrogram_samples_files, self.num_sub_samples)  # sampling without replacement
        for file in files:
            spectrogram = np.load(file)
            assert spectrogram.shape[0] == self.img_height, "Input spectrogram height does not match img height"
            self.sub_samples.append(spectrogram)



class VerificationDataGenerator(BaseDataGenerator):

    def __init__(self, spectrogram_samples_files, batch_size, num_batches, num_sub_samples, img_height):
        self.spectrogram_samples_files = spectrogram_samples_files  # list of filepaths
        self.batch_size = batch_size  # batch size
        self.num_batches = num_batches  # num batches per epoch
        self.num_sub_samples = num_sub_samples  # num sub-samples per epoch
        self.img_height = img_height  # height of square img to be generated in the batches
        self.sub_samples = []  # list of RGB converted spectrograms

    def generate_batches(self):
        # while True:
        self.create_sub_samples()
        # batches per epoch
        for _ in range(self.num_batches):
            # create batch
            labels = []
            query_imgs = []
            candidate_imgs = []
            for _ in range(self.batch_size):
                sample_spectrograms_indices = random.sample(range(self.num_sub_samples), 2)  # sample query/candidate pair
                pos_idx = sample_spectrograms_indices[0]  # positive sample
                neg_idx = sample_spectrograms_indices[1]  # negative sample
                imgs = []
                # Generate query image
                imgs.append(self.get_sliding_img_slice_from_spectrogram(self.sub_samples[pos_idx]))
                # Generate positive candidate
                imgs.append(self.get_sliding_img_slice_from_spectrogram(self.sub_samples[pos_idx]))
                # Generate negative candidate
                imgs.append(self.get_sliding_img_slice_from_spectrogram(self.sub_samples[neg_idx]))
                # Generate labels and append to main list
                # Positive
                labels.append(1); query_imgs.append(imgs[0]); candidate_imgs.append(imgs[1])
                # Negative
                labels.append(0); query_imgs.append(imgs[0]); candidate_imgs.append(imgs[2])
                # Convert to tensor
            labels = torch.tensor(labels)
            input_imgs = torch.tensor([query_imgs, candidate_imgs])
            yield (input_imgs, labels)

    def create_sub_samples(self):
        self.sub_samples = []  # reset
        files = random.sample(self.spectrogram_samples_files, self.num_sub_samples)  # sampling without replacement
        for file in files:
            spectrogram = np.load(file)
            assert spectrogram.shape[0] == self.img_height, "Input spectrogram height does not match img height"
            self.sub_samples.append(spectrogram)


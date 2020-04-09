import os
import pickle
import json

from config import output_data_folder


def main():
    validation_sets_folder = os.path.join(output_data_folder, "validation_sets")

    validation_set_file = os.path.join(validation_sets_folder, "verification_validation_set.pickle")
    with open(validation_set_file, 'rb') as f:
        validation_data = pickle.load(f)

    # Get one spectrogram img
    input_imgs, labels = validation_data[0]
    for i in range(2):
        arr = input_imgs[i][0].numpy()
        print(arr.shape)
        arr = arr.tolist()
        # JSON
        json_str = json.dumps(arr)
        out_file = os.path.join(output_data_folder, "misc", "spectrogram_{}.json".format(i))
        with open(out_file, "w+") as f:
            f.write(json_str)



if __name__ == '__main__':
    main()
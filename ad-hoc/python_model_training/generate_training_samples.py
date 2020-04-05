import os
import random
import json
import pandas as pd
import librosa

from config import audio_dev_folder, audio_test_folder, output_data_folder


def generate_training_dataset_specs(threshold, input_file, output_file, audio_folder):
    ### Speaker level stats
    df = pd.read_csv(input_file, header=0)
    df = df.loc[df["total_files_seconds"] >= threshold, :]
    speakers = df["speaker"].tolist()

    ### Create training dataset
    # Speaker level dataset
    def generate_speaker_dataset(speaker):
        sub_dir1 = os.path.join(audio_folder, "wav", speaker)
        total_files_seconds = 0
        files = []
        for video in os.listdir(sub_dir1):
            sub_dir2 = os.path.join(sub_dir1, video)
            for utterance in os.listdir(sub_dir2):
                file = os.path.join(sub_dir2, utterance)
                total_files_seconds += librosa.core.get_duration(filename=file)
                files.append(r"{0}\{1}".format(video, utterance))
                # termination
                if total_files_seconds >= threshold:
                    dataset = {
                        "files": files,
                        "total_files_seconds": total_files_seconds,
                        "train_test_split": random.random(),   # random number for train test split later
                    }
                    return dataset
    # Create dataset
    datasets_json = {}
    for speaker in speakers:
        dataset = generate_speaker_dataset(speaker)
        datasets_json[speaker] = dataset
        print(speaker)
    # save
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(datasets_json, f, ensure_ascii=False, indent=4)



def main():
    # ### Training data
    # threshold = 500   # total_files_seconds threshold
    # input_file = os.path.join(audio_dev_folder, "speaker_level_stats.csv")   # Get speakers with enough utterances
    # output_file = os.path.join(output_data_folder, "training_dataset_dev.json")   # JSON file for dataset
    # generate_training_dataset_specs(threshold, input_file, output_file, audio_dev_folder)

    ### Test data
    threshold = 300  # total_files_seconds threshold
    input_file = os.path.join(audio_test_folder, "speaker_level_stats.csv")  # Get speakers with enough utterances
    output_file = os.path.join(output_data_folder, "training_dataset_test.json")  # JSON file for dataset
    generate_training_dataset_specs(threshold, input_file, output_file, audio_test_folder)



if __name__ == "__main__":
    main()
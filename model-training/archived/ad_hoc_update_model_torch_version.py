import os
import torch
from torchsummary import summary

import config
from project_utils import load_module_from_file


IMG_HEIGHT = 128
model_folder = os.path.join(r"D:\Desktop\projects\speaker_recognition_voxceleb1\output_data\models\verification_classifier\good_models\updated_pytorch\2020-03-18_23-55-52")


def load_pretrained_encoder_model():
    encoder_model_folder = os.path.join(config.models_folder, "contrastive_encoder", "good_models", "2020-03-18_19-49-46")
    module_file = os.path.join(encoder_model_folder, "model_definitions.py")
    module_name = "MultiSiameseContrastiveClassifierNet"
    module = load_module_from_file(module_file, module_name)
    # load model
    model = module.MultiSiameseContrastiveClassifierNet()
    state_dict_file = os.path.join(encoder_model_folder, "best_model_MultiSiameseContrastiveClassifierNet.pt")
    model.load_state_dict(torch.load(state_dict_file, map_location="cpu"))
    # encoder_model
    encoder_model = model.encoder  # return pretrained encoder only
    for param in encoder_model.parameters(): param.requires_grad = False  # freeze encoder layers
    summary(encoder_model, input_size=(3, IMG_HEIGHT, IMG_HEIGHT), device='cpu')
    return encoder_model


def load_old_model(encoder_model):
    module_file = os.path.join(model_folder, "model_definitions.py")
    module_name = "VerificationBinaryClassifierNet"
    module = load_module_from_file(module_file, module_name)
    # load model
    model = module.VerificationBinaryClassifierNet(encoder_model)
    state_dict_file = os.path.join(model_folder, "best_model_VerificationBinaryClassifierNet.pt")
    model.load_state_dict(torch.load(state_dict_file, map_location="cpu"))
    return model


def main():

    # Load old model
    encoder_model = load_pretrained_encoder_model()

    model = load_old_model(encoder_model)
    # Print summary
    summary(model, input_size=(2, 3, IMG_HEIGHT, IMG_HEIGHT), device='cpu')
    # Save torchscript
    example = [torch.rand(1, 3, IMG_HEIGHT, IMG_HEIGHT), torch.rand(1, 3, IMG_HEIGHT, IMG_HEIGHT)]
    traced_script_module = torch.jit.trace(model, (example,))
    traced_script_module.save(os.path.join(model_folder, "mobile_model_updated.pt"))


if __name__ == "__main__":
    main()












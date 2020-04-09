import torch
import torch.nn as nn
import torch.nn.functional as F
from torchvision import models


class SpectrogramEncoderNet(nn.Module):
    def __init__(self):
        super(SpectrogramEncoderNet, self).__init__()
        self.encoder_size = 128
        # self.encoder = models.mobilenet_v2(pretrained=False)   # base model (transfer learning)
        self.encoder = models.densenet121(pretrained=False)  # base model (transfer learning)
        self.encoder.classifier = nn.Sequential(
            # nn.Linear(1280, self.encoder_size),   # encoding layer, mobile_netV2 output: 1280
            nn.Linear(1024, self.encoder_size),  # encoding layer, densenet121 output: 1024
        )

    def forward(self, input_img):
        return self.encoder(input_img)



class MultiSiameseContrastiveClassifierNet(nn.Module):
    def __init__(self):
        super(MultiSiameseContrastiveClassifierNet, self).__init__()
        self.encoder = SpectrogramEncoderNet()
        self.encoding_projection = nn.Sequential(
            # nn.Tanh(),
            # nn.Linear(self.encoder.encoder_size, self.encoder.encoder_size),   # projection layer
            #             nn.Tanh(),
            # #             nn.ReLU(),
            #             nn.Linear(512, 512),   # projection layer
            nn.Identity(),
        )

    def encode(self, x):
        x = self.encoder(x)
        x = self.encoding_projection(x)
        return x

    def forward(self, input_imgs):
        # cosine sim of query img against each batch img
        query_img_encoding = self.encode(input_imgs[0])  # 1st img is the query img
        cosine_sims = []
        for i in range(1, len(input_imgs)):  # batch imgs
            batch_img_encoding = self.encode(input_imgs[i])
            cosine_sims.append(F.cosine_similarity(query_img_encoding, batch_img_encoding))
        return torch.stack(cosine_sims, dim=1)  # concat cosine sims



class VerificationBinaryClassifierNet(nn.Module):
    def __init__(self, encoder_net):
        super(VerificationBinaryClassifierNet, self).__init__()
        self.encoder = encoder_net
        self.classifier = nn.Sequential(
            nn.Tanh(),
            nn.Linear(self.encoder.encoder_size, 128),
            nn.ReLU(),
            nn.Linear(128, 128),
            nn.ReLU(),
            nn.Linear(128, 128),
            nn.ReLU(),
            nn.Linear(128, 128),
            nn.ReLU(),
            nn.Linear(128, 2),
        )

    def encode(self, x):
        return self.encoder(x)

    def forward(self, input_imgs):
        abs_diff = (self.encode(input_imgs[0]) - self.encode(input_imgs[1])).abs()
        return self.classifier(abs_diff)


# class VerificationBinaryClassifierNet(nn.Module):
#     def __init__(self, encoder_net):
#         super(VerificationBinaryClassifierNet, self).__init__()
#         self.encoder = encoder_net
#         self.classifier = nn.Sequential(
#             # nn.Tanh(),
#             nn.Linear(self.encoder.encoder_size*2, 128),
#             nn.ReLU(),
#             nn.Linear(128, 128),
#             nn.ReLU(),
#             nn.Linear(128, 128),
#             nn.ReLU(),
#             nn.Linear(128, 128),
#             nn.ReLU(),
#             nn.Linear(128, 2),
#         )
#
#     def encode(self, x):
#         return self.encoder(x)
#
#     def forward(self, input_imgs):
#         concat = torch.cat((self.encode(input_imgs[0]), self.encode(input_imgs[1])), 1)
#         return self.classifier(concat)




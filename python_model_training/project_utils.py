import os
import logging
from datetime import datetime

import torch


'''
Creates folder to save best model during training
print() logs the string into a log file before printing on console  
'''
class ModelSaveAndLogHandler:
    def __init__(self, model_save_folder, enable_model_saving=True, enable_logging=True):
        self.enable_model_saving = enable_model_saving
        self.enable_logging = enable_logging
        self.folder = self.__create_session_folder(model_save_folder) if enable_logging else None
        self.logger = self.__setup_logger(self.folder) if enable_logging else None

    def save_pytorch_model(self, model, model_file_name):
        if self.enable_model_saving:
            # torch.save(model, os.path.join(self.folder, model_file_name))
            torch.save(model.state_dict(), os.path.join(self.folder, model_file_name))
            self.print("MODEL SAVED")

    def print(self, msg=""):
        if self.enable_logging: self.logger.info(msg)
        print(msg)


    ### Init methods

    def __create_session_folder(self, model_save_folder):
        dt_str = datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
        folder = os.path.join(model_save_folder, dt_str)
        if not os.path.exists(folder): os.makedirs(folder)
        return folder

    def __setup_logger(self, folder):
        logging.basicConfig(
            filename=os.path.join(folder, "print.log"),
            filemode='a+',
            format='%(asctime)s %(name)s %(levelname)s -- %(message)s',
            datefmt='%Y-%m-%d_%H:%M:%S',
            level=logging.INFO
        )
        logger = logging.getLogger(self.__class__.__name__)
        return logger
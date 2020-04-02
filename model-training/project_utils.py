import os
import logging
import shutil
import sys
import copy
import importlib.util
from datetime import datetime

import torch


'''
Creates folder to save best model during training
>> save_pytorch_model() saves pytorch model state dict
>> save_model_definition_file() saves the model class definition file of the model (for future reloading)
>> print() logs the string into a log file before printing on console  
'''
class ModelSaveAndLogHandler:
    def __init__(self, model_save_folder, enable_model_saving=True, enable_logging=True):
        self.enable_model_saving = enable_model_saving
        self.enable_logging = enable_logging
        self.folder = self.__create_session_folder(model_save_folder) if enable_logging else None
        self.logger = self.__setup_logger(self.folder) if enable_logging else None

    def save_pytorch_model(self, model, model_file_name):
        if self.enable_model_saving:
            torch.save(model.state_dict(), os.path.join(self.folder, model_file_name))
            self.print("MODEL SAVED")

    def save_pytorch_model_as_torchscript(self, model, model_file_name, example):
        if self.enable_model_saving:
            model = copy.deepcopy(model)
            model.to("cpu")
            model.eval()
            traced_module = torch.jit.trace(model, example)
            traced_module.save(os.path.join(self.folder, model_file_name))
            self.print("MODEL SAVED (MOBILE)")

    def save_model_definition_file(self, model_def_src_file_path):
        if self.enable_model_saving:
            file_name = os.path.basename(model_def_src_file_path)
            dst_file_path = os.path.join(self.folder, file_name)
            shutil.copy2(model_def_src_file_path, dst_file_path)

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



'''
Loads module from python file with importlib  
'''
def load_module_from_file(module_file, module_name):
    spec = importlib.util.spec_from_file_location(module_name, module_file)
    module = importlib.util.module_from_spec(spec)
    sys.modules[module_name] = module
    spec.loader.exec_module(module)
    return module

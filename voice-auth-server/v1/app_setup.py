import os


WORKING_FOLDER = os.path.dirname(os.path.realpath(__file__))

TEMP_FOLDER = os.path.join(WORKING_FOLDER, "temp")
if not os.path.exists(TEMP_FOLDER): os.makedirs(TEMP_FOLDER)
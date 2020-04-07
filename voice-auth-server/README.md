# Flask application

## With virtualenv
How to run (tested on MacOS):

1. Create a virtualenv by using 
```virtualenv -p python3 env```

2. Active env (for MacOS) 
```source env/bin/activate```

3. pip3 install packages from requirements.txt 
```pip3 install -r requirements.txt```

4. Run server 
```python3 application.py``` 



## With conda
How to run with conda env (tested on Windows):

1. Create a conda env with the dependencies by using   
```conda env create -n voice_auth_pytorch --file environment.yml```

2. Active env 
```conda activate voice_auth_pytorch```

3. Run server 
```python application.py```
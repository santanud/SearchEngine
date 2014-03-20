import sys
import math
import re
import numpy as np
from sklearn import linear_model, svm
from sklearn.externals import joblib

feature_file = str(sys.argv[1])
label_file = str(sys.argv[2])
model_type = str(sys.argv[3])

#label_file = '/cs276/PA4/release/t2_temp_label.txt'
#feature_file ='/cs276/PA4/release/t2_temp_Feature.txt'
#model_type = 'SVM'

base_path='/cs276/'

t2_mean_file = base_path + 'PA4/release/t2_mean.txt'
t2_sd_file = base_path + 'PA4/release/t2_sd.txt'

t3_mean_file = base_path + 'PA4/release/t3_mean.txt'
t3_sd_file = base_path + 'PA4/release/t3_sd.txt'

t4_mean_file = base_path + 'PA4/release/t4_mean.txt'
t4_sd_file = base_path + 'PA4/release/t4_sd.txt'

X_data = np.loadtxt(feature_file,delimiter='\t',dtype=float)

if model_type == 'LINEAR': 
    
    filename = base_path + 'PA4/release/Linear.Model.joblib.pkl'
    regr = joblib.load(filename)
    Y_bar = regr.predict(X_data)

elif model_type == 'SVM':     
    
      filename = base_path + 'PA4/release/SVM.Model.joblib.pkl'
      t2_mn = np.loadtxt(t2_mean_file,delimiter='\t',dtype=float)
      t2_sd = np.loadtxt(t2_sd_file,delimiter='\t',dtype=float)
      X = (X_data - t2_mn)/t2_sd
    
      svc = joblib.load(filename)
      Y_bar = svc.predict(X)
      #print X.shape
      if X.ndim != 1:
          for i in range(X.shape[0]):
              if np.sum(svc.coef0 +X[i,:] * svc.coef_) > 0:
                  Y_bar[i] = 1
              else:
                  Y_bar[i] = -1
      #print Y_bar
      
elif model_type == 'SVMEXTENDED':
    
     filename = base_path + 'PA4/release/SVM.Ext.Model.joblib.pkl'
     clf = joblib.load(filename)
     
     t3_mn = np.loadtxt(t3_mean_file,delimiter='\t',dtype=float)
     t3_sd = np.loadtxt(t3_sd_file,delimiter='\t',dtype=float)
     X = (X_data - t3_mn)/t3_sd
     
     Y_bar = clf.predict(X)
     if X.ndim != 1:
         for i in range(X.shape[0]):
             if np.sum(clf.coef0 +X[i,:] *clf.coef_) > 0:
                 Y_bar[i] = 1
             else:
                 Y_bar[i] = -1
 
    
elif model_type == 'SVMREGRESSION':
    
     filename = base_path + 'PA4/release/SVM.Regr.Model.joblib.pkl'
     svr = joblib.load(filename)
     t4_mn = np.loadtxt(t4_mean_file,delimiter='\t',dtype=float)
     t4_sd = np.loadtxt(t4_sd_file,delimiter='\t',dtype=float)
     X = (X_data - t4_mn)/t4_sd
     
     Y_bar = svr.predict(X)
     
     
     
else:
    print 'wrong value'
    exit(1)
    
file=open(label_file,'w')
for x in np.nditer(Y_bar):
    file.write(str(x)+'\n')
file.close()








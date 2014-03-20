import sys
import math
import re
import numpy as np
from sklearn import linear_model, svm, grid_search 
from sklearn.metrics import classification_report , precision_score, recall_score
from sklearn.externals import joblib
from sklearn.cross_validation import KFold

feature_file = str(sys.argv[1])
label_file = str(sys.argv[2])
model_type = str(sys.argv[3])

#label_file = '/cs276/PA4/release/task2_label.txt'
#feature_file ='/cs276/PA4/release/task2_train.txt'
#model_type = 'SVM'

base_path='/cs276/'
#base_path='/afs/.ir.stanford.edu/users/c/o/conradr/'

t2_mean_file = base_path + 'PA4/release/t2_mean.txt'
t2_sd_file = base_path + 'PA4/release/t2_sd.txt'

t3_mean_file = base_path + 'PA4/release/t3_mean.txt'
t3_sd_file = base_path + 'PA4/release/t3_sd.txt'

t4_mean_file = base_path + 'PA4/release/t4_mean.txt'
t4_sd_file = base_path + 'PA4/release/t4_sd.txt'


X_data = np.loadtxt(feature_file,delimiter='\t',dtype=float)
Y_data = np.loadtxt(label_file,delimiter='\t',dtype=float)

#mn = np.mean(X_data,axis=0)
#sd = np.std(X_data,axis=0)
#X = (X_data - mn)/sd

if model_type == 'LINEAR':
    kf = KFold(len(Y_data), k=5, indices=False)
    print kf
    best_score =0.0;
    for train_index, test_index in kf:
        #print("TRAIN:", train_index, "TEST:", test_index)
        X_train, X_test = X_data[train_index], X_data[test_index]
        y_train, y_test = Y_data[train_index], Y_data[test_index]
        regr = linear_model.LinearRegression()
        regr.fit(X_train,y_train)
        print 'Train score : ',regr.score(X_train,y_train), 'Test score : ',regr.score(X_test,y_test)
        new_test_score = regr.score(X_test,y_test)
        if best_score < new_test_score :
            best_score = new_test_score
            best_train_index = train_index
    X_train_best, y_train_best = X_data[best_train_index], Y_data[best_train_index]
    regr.fit(X_train_best,y_train_best)
    print 'Final Train score : ',regr.score(X_train_best,y_train_best), 'Final Test score : ',regr.score(X_data,Y_data)
        
    print('Coef : '+ str(regr.coef_))   
    print('Intercept : '+ str(regr.intercept_))  
    
    filename = base_path + 'PA4/release/Linear.Model.joblib.pkl'
    _ = joblib.dump(regr, filename, compress=9)

    #regr2 = joblib.load(filename)

elif model_type == 'SVM': 

    t2_mn = np.mean(X_data,axis=0)
    t2_sd = np.std(X_data,axis=0)
    np.savetxt(t2_mean_file, t2_mn, delimiter='\t')
    np.savetxt(t2_sd_file, t2_sd, delimiter='\t')
    X = (X_data - t2_mn)/t2_sd
    
    kf = KFold(len(Y_data), k=5, indices=False)
    print kf
    best_score =0.0;
    for train_index, test_index in kf:
        X_train, X_test = X[train_index], X[test_index]
        y_train, y_test = Y_data[train_index], Y_data[test_index]
        clf = svm.SVC(kernel='linear')
        clf.fit(X_train,y_train)
        print 'Train score : ',clf.score(X_train,y_train), 'Test score : ',clf.score(X_test,y_test)
        new_test_score = clf.score(X_test,y_test)
        if best_score < new_test_score :
            best_score = new_test_score
            best_train_index = train_index
            best_test_index = test_index
    X_train_best, y_train_best = X[best_train_index], Y_data[best_train_index]
    X_test_best, y_test_best = X[best_test_index], Y_data[best_test_index]

    clf.fit(X_train_best,y_train_best)
    print 'Final Train score : ',clf.score(X_train_best,y_train_best), 'Final Test score : ',clf.score(X_test_best,y_test_best)
    print 'Overall Train score : ',clf.score(X,Y_data)

    print clf
    filename = base_path + 'PA4/release/SVM.Model.joblib.pkl'
    joblib.dump(clf, filename, compress=9)
    
    Y_true, Y_bar = Y_data, clf.predict(X)
    print classification_report(Y_true, Y_bar)
    
    
elif model_type == 'SVMEXTENDED':
    t3_mn = np.mean(X_data,axis=0)
    t3_sd = np.std(X_data,axis=0)
    np.savetxt(t3_mean_file, t3_mn, delimiter='\t')
    np.savetxt(t3_sd_file, t3_sd, delimiter='\t')
    X = (X_data - t3_mn)/t3_sd
    clf = svm.SVC(kernel='linear')
    clf.fit(X,Y_data)
    
    filename = base_path + 'PA4/release/SVM.Ext.Model.joblib.pkl'
    joblib.dump(clf, filename, compress=9)
    print clf
    Y_true, Y_bar = Y_data, clf.predict(X)
    print classification_report(Y_true, Y_bar)
    

elif model_type == 'SVMREGRESSION':
    print 'SVM Regression'
    t4_mn = np.mean(X_data,axis=0)
    t4_sd = np.std(X_data,axis=0)
    np.savetxt(t4_mean_file, t4_mn, delimiter='\t')
    np.savetxt(t4_sd_file, t4_sd, delimiter='\t')
    X = (X_data - t4_mn)/t4_sd
  
    clf = svm.SVR(kernel='linear')
    clf.fit(X,Y_data)
     # clf.predict([[1, 1]])
    filename = base_path + 'PA4/release/SVM.Regr.Model.joblib.pkl'
    joblib.dump(clf, filename, compress=9)
     
    
else:
    print 'wrong value'
    exit(1)










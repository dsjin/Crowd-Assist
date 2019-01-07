"""
Name : Decision Tree with Firebase Admin 
Author : Thatchakon Jom-ud & Tanawat Kusungnone
"""
import firebase_admin
from firebase_admin import db as rdb
from firebase_admin import firestore
from firebase_admin import credentials
import datetime as dt
import pytz
import pandas as pd
import numpy as np
from sklearn import tree
from sklearn import preprocessing
import sys
set(pytz.all_timezones_set)
cred = credentials.Certificate('key.json')
firebase_admin.initialize_app(cred, {
        'databaseURL': 'https://senior-project-it.firebaseio.com'
})
le = preprocessing.LabelEncoder()
db = firestore.client()
ref = rdb.reference('area')

crime_mapper = {'low':2, 'normal':1, 'high':0}
density_mapper = {'low':1, 'high':0}
regions = ['region1']
divisions = {'region1':['division1','division2','division3','division4','division5','division6','division7','division8','division9']}

def initTree():
    df = pd.read_csv("csv/area_estimate_data.csv")
    Y = df.pop("risk_level")
    df['crime_frequency'] = df.crime_frequency.astype("category")
    df['time'] = df.time.astype("category")
    df['density'] = df.density.astype("category")
    df.pop("area")
    le.fit(df.time)
    df.time = le.transform(df.time)
    df.crime_frequency = df.crime_frequency.replace(crime_mapper)
    df.density = df.density.replace(density_mapper)
    return tree.DecisionTreeClassifier(criterion="entropy").fit(df, Y)

def predict():
    tree_model = initTree()
    now = dt.datetime.now(pytz.timezone('Asia/Bangkok'))
	time = ["day","night"][now.hour == 22]
    for region in regions:
        for division in divisions[region]:
            data = db.collection(u'decision_tree_data').document(region).collection(division).document(u'data').get()
            data_predict = [('crime_frequency', [crime_mapper[data.to_dict()['crime_level']]]),
                ('time', [time]),
                ('density', [density_mapper[data.to_dict()['density_level']]])]
            data_predict = pd.DataFrame.from_items(data_predict)
            data_predict["crime_frequency"] = data_predict.crime_frequency.astype("int64")
            data_predict["time"] = data_predict.time.astype("category")
            data_predict["density"] = data_predict.density.astype("int64")
            data_predict.time = le.transform(data_predict.time)
            result = tree_model.predict(data_predict)[0]
            ref.child('metropolis_'+division).set(result)
    print "Risk Area updated successfully"
predict()
sys.exit(0)
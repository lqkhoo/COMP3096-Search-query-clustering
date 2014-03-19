"""
Flask-based http handler for localhost mongodb instance

Dependencies:
	flask					(http://flask.pocoo.org/)
		jinja2				(http://jinja.pocoo.org/)
			markupsafe		(https://pypi.python.org/pypi/MarkupSafe)
			itsdangerous	(https://pypi.python.org/pypi/itsdangerous)
		werkzeug			(http://werkzeug.pocoo.org/docs/)
	pymongo					(https://pypi.python.org/pypi/pymongo/)

@author Li Quan Khoo
"""

import cgi
from flask import *

from Core import Core
from MongoWriter import MongoWriter

# Settings
SETTINGS = {
	'DEBUG': True,
    'SERVER': {
        'HOST': '127.0.0.1',
        'PORT': 80
    },
	'MONGO': {
		'HOST': '127.0.0.1',
		'PORT': 27017,
		'DB_NAME': 'yago2'
	}
}

# Globals
app = Flask(__name__)
core = Core(SETTINGS)

# Path ----------------------------------------------------------------------------------

@app.route('/', methods=['GET'])
def root():
	return render_template('index.html')


@app.route('/explore', methods=['GET'])
def explore():
	return render_template('explore.html')


@app.route('/api/entities', methods=['GET'])
def api_entities():
	
	args = request.args.to_dict()
	
	if('rawSearchString' in args):
		entitiesDict = core.getEntities(args['rawSearchString'])	# Find all the relevant entities
		dataDict = core.getSimilarities(entitiesDict)				# Calculate similarity
		return jsonify(dataDict)
	
	# No arguments supplied -- return empty list
	return jsonify([])

@app.route('/api/classes', methods=['GET'])
def api_classes():
	
	args = request.args.to_dict()
	
	if('name' in args):
		mongoWriter = MongoWriter(SETTINGS)
		return jsonify(mongoWriter.getClassByClassName(args['name']))
	
	return None


# Helpers -------------------------------------------------------------------------------

# Serialize MongoDB bson object to Json with cgi escaping (HTML escaping)
def bsonToJson(bsonObj):
	# del bsonObj['_id'] # remove the mongoDB internal unserializable bson _id object
	return cgi.escape(json.dumps(bsonObj))


# Run
if __name__ == '__main__':
	app.run(
		debug = SETTINGS['DEBUG'],
		host = SETTINGS['SERVER']['HOST'],
		port = SETTINGS['SERVER']['PORT']
		)
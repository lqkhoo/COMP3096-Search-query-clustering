"""
Core class for backend MongoDB operations

@author Li Quan Khoo
"""

from pymongo import MongoClient

class MongoWriter():
	
	def __init__(self, settingsDict):
		
		self.SETTINGS = settingsDict
		
		self.mongoclient = MongoClient(
							self.SETTINGS['MONGO']['HOST'],
							self.SETTINGS['MONGO']['PORT']
							)
		
		self.db = self.mongoclient[self.SETTINGS['MONGO']['DB_NAME']]
	
	
	"""
	Strips _id field from document returned by mongo driver which is unserializable to json
	@param bsonObj {?}
	"""
	def stripBsonId(self, bsonObj):
		del bsonObj['_id']
		return bsonObj
	
	"""
	"""
	def getSearchMaps(self, searchString):
		return self.stripBsonId(self.db['searchMaps'].find_one({'searchString': searchString}))
	
	
	"""
	@param searchString {String} searchString of the entity
	"""
	def getEntities(self, searchString):
		
		entityDocuments = []
		cursor = self.db['entities'].find({'searchString': searchString})
		
		if cursor.count() != 0:
			for record in cursor:
				record = self.stripBsonId(record)
				entityDocuments.append(record)
		
		return entityDocuments
	
	
	"""
	@param className {String}
	"""
	def getClassByClassName(self, className):
		return self.stripBsonId(self.db['classes'].find_one({'name': className}))
	
	
	"""
	@param className {String}
	"""
	def getClassToEntityMapping(self, className):
		result = self.db['clusterMappingsClassToEntity'].find_one({'name': className})
		if(result != None):
			return self.stripBsonId(result)
		else:
			return None
	
	
	"""
	@param entityName {String}
	"""
	def getEntityToEntityMapping(self, entityName):
		
		result = self.db['clusterMappingsEntityToEntity'].find_one({'name': entityName})
		if(result != None):
			return self.stripBsonId(result)
		else:
			return None
		
	
	"""
	@param className {String}
	"""
	def getClassToStringMapping(self, className):
		print className
		result = self.db['clusterMappingsClassToString'].find_one({'name': className})
		if(result != None):
			return self.stripBsonId(result)
		else:
			return None
	
	
"""
Core class for backend logic

@author Li Quan Khoo
"""

from stemmer import PorterStemmer
from MongoWriter import MongoWriter

class Core():
	
	def __init__(self, settingsDict):
		
		self.SETTINGS = settingsDict
		
		self.mongoWriter = MongoWriter(settingsDict)
		
		self.porterStemmer = PorterStemmer()
		
		self.STOPWORDS = [
			 'i',
			 'a',
			 'about',
			 'an',
			 'are',
			 'as',
			 'at',
			 'be',
			 'by',
			 'com',
			 'for',
			 'from',
			 'how',
			 'in',
			 'is',
			 'it',
			 'of',
			 'on',
			 'or',
			 'that',
			 'the',
			 'this',
			 'to',
			 'was',
			 'what',
			 'when',
			 'where',
			 'who',
			 'will',
			 'with',
			 'the',
			 'www'
		]
	
	"""
	This is a direct copy of the method generateQuerySubstrings in src/processor/QueryMapper.java
	"""
	def generateQuerySubstrings(self, rawSubstring):
		
		parts = None
		substring = None
		substrings = []
		
		# degenerate condition
		if(rawSubstring == ""):
			return []
		
		parts = rawSubstring.lower().split(" ")
		
		j = len(parts) - 1
		while j >= 0:
			
			i = 0
			while i + j < len(parts):
				
				substring = ""
				
				k = i
				while k <= i + j:
					if substring == "":
						substring += parts[k]
					else:
						substring += " " + parts[k]
					k += 1
				
				substrings.append(substring)
				i += 1
				
			j -= 1
			
		return substrings
	
	
	
	"""
	Runs the porter stemmer library on the given string and returns the stemmed string
	"""
	def _stem(self, string):
		return ''.join(self.porterStemmer.stem(list(string), 0, len(string) - 1))
	
	
	
	"""
	Fetches relevant entities matching rawQueryString or its substrings
	"""
	def getEntities(self, rawQueryString):
				
		def _removeLesserSubstrings(array, substring):
			subSubstrings = self.generateQuerySubstrings(substring)
			for subsubString in subSubstrings:
				if subsubString in array:
					array.remove(subsubString)
		
		
		dict = {}
		stemmedSubstring = None
		querySubstringArray = self.generateQuerySubstrings(rawQueryString)
		
		# Algorithm assumes substrings sorted from longest to shortest, left to right
		i = 0
		while i < len(querySubstringArray):
			substring = querySubstringArray[i]
			entityDocuments = self.mongoWriter.getEntities(substring)
			if len(entityDocuments) != 0 :
				dict[substring] = entityDocuments
				_removeLesserSubstrings(querySubstringArray, substring)
				i -= 1
			else:
				stemmedSubstring = self._stem(substring)
				entityDocuments = self.mongoWriter.getEntities(stemmedSubstring)
				if len(entityDocuments) != 0:
					dict[stemmedSubstring] = entityDocuments
					_removeLesserSubstrings(querySubstringArray, stemmedSubstring)
					i -= 1
			
			i += 1
			
		return dict
	
	
	"""
	
	"""
	def getSimilarities(self, entitiesDict):
		
		def getClasses(entity):
			classes = []
			if 'relations' in entity:
				relations = entity['relations']
				if 'rdf:type' in relations:
					classes = relations['rdf:type']
			return classes
		
		def getLinks(entity):
			links = []
			if 'relations' in entity:
				relations = entity['relations']
				if '<linksTo>' in relations:
					links = relations['<linksTo>']
			return links
		
		# Values from the java extractor modules
		COMMON_CLASS_WEIGHT = 1
		COMMON_LINK_WEIGHT = 0.2
		
		SIMILARITY_CULL_THRESHOLD = 5
		
		keys = entitiesDict.keys()
		
		
		entityGroup1 = None
		entityGroup2 = None
		entity1Classes = None
		entity2Classes = None
		entity1Links = None
		entity2Links = None
		classIntersection = None
		linksIntersection = None
		similarityScore = None
		
		dataDict = {}
		similarityDataArray = []
		similarityDict = None
		
		# Handshake. Compare each pair once only
		i = 0
		while i < len(keys):
			entityGroup1 = entitiesDict[keys[i]]
			
			j = 0
			while j < len(keys) and j < i:
				entityGroup2 = entitiesDict[keys[j]]
				
				for entity1 in entityGroup1:
					entity1Classes = getClasses(entity1)
					entity1Links = getLinks(entity1)
					
					for entity2 in entityGroup2:
						entity2Classes = getClasses(entity2)
						entity2Links = getLinks(entity2)
						classIntersection = list(set(entity1Classes).intersection(set(entity2Classes)))
						linksIntersection = list(set(entity1Links).intersection(set(entity2Links)))
						similarityScore = COMMON_CLASS_WEIGHT * len(classIntersection) + COMMON_LINK_WEIGHT * len(linksIntersection)
						
						if similarityScore > SIMILARITY_CULL_THRESHOLD:
							similarityDict = {}
							similarityDict['entity1'] = entity1['name']
							similarityDict['entity2'] = entity2['name']
							similarityDict['similarityScore'] = similarityScore
							similarityDict['commonClasses'] = classIntersection
							similarityDict['commonLinks'] = linksIntersection 
							
							similarityDataArray.append(similarityDict)
				
				j += 1
				
			i += 1
		
		dataDict['entities'] = entitiesDict
		dataDict['similarities'] = sorted(similarityDataArray,
										key = lambda similarity: similarity['similarityScore'],
										reverse = True)
		
		return dataDict


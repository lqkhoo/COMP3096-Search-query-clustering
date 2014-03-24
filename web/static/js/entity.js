/**
 * d3 visualizer module for clusters / entities explorer
 * @author Li Quan Khoo
 */
var APP = APP || {};
(function($, d3) {
    
    if (! $)        throw error('jQuery not found.');
    if (! d3)       throw error('d3 not found');
    
    APP.DEBUG = true;
    APP.api = {
        getClassDataByClassName: function(className, callback) {
            $.ajax({
                type: 'GET',
                url: '/api/classes',
                data: {name: className},
                dataType: 'json',
                success: callback,
                error: function(jqXHr, textStatus, errorThrown) {
                    if (APP.DEBUG) {
                        console.log('getClassDataByClassName AJAX error');
                        console.log(jqXHr);
                        console.log(textStatus);
                        console.log(errorThrown);
                    }
                }
            });
        },
        getClassToEntityMappingByClassName: function(className, callback) {
            $.ajax({
                type: 'GET',
                url: '/api/classtoentity',
                data: {name: className},
                dataType: 'json',
                success: callback,
                error: function(jqXHr, textStatus, errorThrown) {
                    if (APP.DEBUG) {
                        console.log('getClassToEntityMappingByClassName AJAX error');
                        console.log(jqXHr);
                        console.log(textStatus);
                        console.log(errorThrown);
                    }
                }
            });
        },
        getEntityToEntityMappingByEntityName: function(entityName, callback) {
            $.ajax({
                type: 'GET',
                url: '/api/entitytoentity',
                data: {name: entityName},
                dataType: 'json',
                success: callback,
                error: function(jqXHr, textStatus, errorThrown) {
                    if (APP.DEBUG) {
                        console.log('getEntityToEntityMappingByClassName AJAX error');
                        console.log(jqXHr);
                        console.log(textStatus);
                        console.log(errorThrown);
                    }
                }
            });
        }
    };
    
    return APP;
    
}(jQuery, d3));

var ajaxDataCache = {
        classes: {},
        entities: {}
    };

$(document).ready(function() {
    
    var width = $(window).width();
    var height = $(window).height();
    var centerX = width / 2;
    var centerY = height / 2;
    
    
    // Hash of node names to graphDataObj indices for efficient seeks
    var graphDataHash = {
        'class': {},
        'entity': {},
        'links': {}
    };
    
    var graphDataObj = {
        nodes: [],
        links: []
    };
    
    var svg = d3.select('#container').append('svg')
        .attr('width', width)
        .attr('height', height)
        .attr("viewBox", "0 0 " + width + " " + height )
        .attr("preserveAspectRatio", "xMidYMid meet")
        .call(d3.behavior.zoom().scaleExtent([0.1, 3]).on("zoom", zoom));
        
    var vis = svg.append('svg:g');
    function zoom() {
        vis.attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale +")");
    };
    
    var group;
    var circle;
    var enter;
    var link;
    
    var force = d3.layout.force()
        .nodes(graphDataObj.nodes)
        .links(graphDataObj.links)
        .charge(-4000)
        .linkDistance(50)
        .friction(0.3)
        .size([width, height]);
        
    var color = d3.scale.category10();
    
    function getColor(d) {
        switch(d) {
        case 'class':
            return 0;
            break;
        case 'entity':
            return 1;
            break;
        default:
            return 2;
            break;
        }
    }
    
    function start() {
        
        var data;
        
        link = vis.selectAll('.link');
        /*
        console.log(graphDataObj.nodes);
        console.log(graphDataObj.links);
        console.log(graphDataHash);
        */
                
        group = vis.selectAll('g.gnode');
        data = group.data(graphDataObj.nodes, function(d) {
            return graphDataHash[d.dataType][d.name];
        });
        enter = data.enter()
            .append('g')
            .attr('class', 'gnode')
            .on('click', function(d) {
                _getD3ClassDataByName(d.name, false);
            })
            .call(force.drag);
        
        enter.append('circle')
            .attr('class', 'node')
            .attr('r', 10)
            .style('fill', function(d) { return color(getColor(d.dataType)); });
        
        enter.append('text')
            .style('font-size', '10px')
            .text(function(d) { return d.name; });
        
        data.exit().remove();
        
        data = link.data(graphDataObj.links, function(d) {
            return graphDataHash.links[d.source.name][d.target.name];
        });
        data.enter()
            .append('line')
            .attr('class', 'link')
            .attr('marker-end', 'url(#end)')
            .style('stroke-width', function(d) { return Math.sqrt(d.value); });
        data.exit().remove();
                
        force.on('tick', function() {
            link.attr('x1', function(d) { return d.source.x; })
                .attr('y1', function(d) { return d.source.y; })
                .attr('x2', function(d) { return d.target.x; })
                .attr('y2', function(d) { return d.target.y; });
            
            group.attr('transform', function(d) {
                return 'translate(' + [d.x, d.y] + ')'; 
            });
        });
        
        force.start();
    }
            
    function _extendGraph(nodes, linkArgs) {
        
        function _addNode(node) {
            
            if(graphDataHash[node.dataType].hasOwnProperty(node.name)) {
                // do nothing - if node already exists then just let it be
            } else {

                graphDataObj.nodes.push(node);
                graphDataHash[node.dataType][node.name] = graphDataObj.nodes.length - 1;
            }
        }
        
        function _addLink(linkArgs) {
            
            var sourceName = linkArgs.sourceName;
            var sourceDataType = linkArgs.sourceDataType;
            var targetName = linkArgs.targetName;
            var targetDataType = linkArgs.targetDataType;
            var weight = linkArgs.weight;
            
            var link;
            
            // if both nodes we are linking exists
            if(graphDataHash[sourceDataType].hasOwnProperty(sourceName) && graphDataHash[targetDataType].hasOwnProperty(targetName)) {
                
                // create hashtables if not hashed
                if(! graphDataHash.links[sourceName]) {
                    graphDataHash.links[sourceName] = {};
                }
                if(! graphDataHash.links[targetName]) {
                    graphDataHash.links[targetName] = {};
                }
                
                if(! graphDataHash.links[sourceName].hasOwnProperty(targetName) || ! graphDataHash.links[targetName].hasOwnProperty[sourceName]) {
                    graphDataHash.links[sourceName][targetName] = graphDataObj.links.length - 1;
                    graphDataHash.links[targetName][sourceName] = graphDataObj.links.length - 1;
                    
                    link = {
                        source: graphDataObj.nodes[graphDataHash[sourceDataType][sourceName]],
                        target: graphDataObj.nodes[graphDataHash[targetDataType][targetName]],
                        weight: weight
                    };
                    
                    graphDataObj.links.push(link);
                } else {
                    console.log('else');
                }
            } else {
                console.log('!!!!!!!!');
                // at least one of the nodes do not exist. We need to query the db for this node before we can add the link.
                // This in principle shouldn't happen
                
            }
        }
        
        var i;
        for(i = 0; i < nodes.length; i++) {
            _addNode(nodes[i]);
        }
        for(i = 0; i < linkArgs.length; i++) {
            _addLink(linkArgs[i]);
        }
    }
    
    // Data retrieval functions
    
    function _getD3ClassDataByName(className, isSourceRoot) {
        
        _getD3SubclassDataByName(className, isSourceRoot, function(data) {
            var i;
            for(i = 0; i < data.length; i++) {
                _getD3SuperclassDataByName(data[i]);
                _getD3ClassToEntityMappingByName(data[i], function(data) {
                    var i;
                    for(i = 0; i < data.length; i++) {
                        _getD3EntityToEntityMappingByName(data[i]);
                    }
                });
            }
            
        });
        
    }
    
    
    function _getD3EntityToEntityMappingByName(data) {
        
        function exec(data) {
            
            if(! data.hasOwnProperty('mappings')) {
                return; // no entity maps to class -- do nothing
            }
            
            var nodes = [];
            var linkArgs = [];
            
            var target;
            var source;
            
            var i;
            for(i = 0; i < data['mappings'].length; i++) {
                source = {
                    name: data['name'],
                    size: 1,
                    dataType: 'entity'
                };
                nodes.push(source);
                
                target = {
                    name: data['mappings'][i]['name'],
                    size: 1,
                    dataType: 'entity'
                };
                
                nodes.push(target);
                
                linkArgs.push({
                    sourceName: data['name'],
                    sourceDataType: 'entity',
                    targetName: data['mappings'][i]['name'],
                    targetDataType: 'entity',
                    weight: 1
                });
            }
            
            _extendGraph(nodes, linkArgs);
            start();
            
        }
        
        if(ajaxDataCache['entities'].hasOwnProperty(data['name'])) {
            exec(ajaxDataCache['classes'][data['name']]);
            return;
        }
        
        APP.api.getEntityToEntityMappingByEntityName(data['name'], function(data) { // cache miss - async call
            ajaxDataCache['entities'] = data;
            exec(data);
        });
        
    }
    
    
    function _getD3ClassToEntityMappingByName(className, callback) {
                
        function exec(data) {
            
            if(! data.hasOwnProperty('mappings')) {
                return; // no entity maps to class -- do nothing
            }
            
            var nodes = [];
            var linkArgs = [];
            
            var target;
            var source;
            
            var i;
            for(i = 0; i < data['mappings'].length; i++) {
                source = {
                    name: className,
                    size: 1,
                    dataType: 'class'
                };
                nodes.push(source);
                
                target = {
                    name: data['mappings'][i]['name'],
                    size: 1,
                    dataType: 'entity'
                };
                
                nodes.push(target);
                
                linkArgs.push({
                    sourceName: className,
                    sourceDataType: 'class',
                    targetName: data['mappings'][i]['name'],
                    targetDataType: 'entity',
                    weight: 1
                });
            }
            
            _extendGraph(nodes, linkArgs);
            start();
            
            if(callback) {
                callback(data['mappings']);
            }
            
            
        }
        
        if(ajaxDataCache['classes'].hasOwnProperty(className)) {
            if(ajaxDataCache['classes'][className].hasOwnProperty('entityMappings')) {
                exec(ajaxDataCache['classes'][className]['entityMappings']);
                return;
            }
        }
        
        APP.api.getClassToEntityMappingByClassName(className, function(data) { // cache miss - async call
            ajaxDataCache['classes'][className]['entityMappings'] = data;
            exec(data);
        });
    }
    
    
    
    function _getD3SubclassDataByName(className, isSourceRoot, callback) {
        
        function exec(data) {
            var nodes = [];
            var linkArgs = [];
            
            var subClassName;
            var source;
            var target;
            var i;
            
            source = {
                name: className,
                x: centerX,
                y: centerY,
                size: Math.sqrt(data['subclassNames'].length),
                dataType: 'class',
            };
            if(isSourceRoot) {
                source.fixed = true;
                source.isRoot = true;
            }
            nodes.push(source);
            for(i = 0; i < data['subclassNames'].length; i++) {
                subClassName = data['subclassNames'][i];
                target = {
                    name: subClassName,
                    size: 1,
                    dataType: 'class'
                };
                nodes.push(target);
                linkArgs.push({
                    sourceName: className,
                    sourceDataType: 'class',
                    targetName: subClassName,
                    targetDataType: 'class',
                    weight: 1
                });
            }
            
            _extendGraph(nodes, linkArgs);
            start();
            
            if(callback) {
                callback(data['subclassNames']);
            }
            
        }
        
        if(ajaxDataCache.hasOwnProperty(className)) {
            exec(ajaxDataCache['classes'][className]); // cache hit
        } else {
            APP.api.getClassDataByClassName(className, function(data) { // cache miss - async call
                ajaxDataCache['classes'][className] = data;
                exec(data);
            });
        }
    }
    
    
    
    function _getD3SuperclassDataByName(className, callback) {
        
        function exec(data) {
            
            var nodes = [];
            var linkArgs = [];
            var superClassName;
            
            var i;
            for(i = 0; i < data['superclassNames'].length; i++) {
                superClassName = data['superclassNames'][i];
                target = {
                    name: superClassName,
                    size: 1,
                    dataType: 'class'
                };
                nodes.push(target);
                linkArgs.push({
                    sourceName: superClassName,
                    sourceDataType: 'class',
                    targetName: className,
                    targetDataType: 'class',
                    weight: 1
                });
            }
            
            _extendGraph(nodes, linkArgs);
            start();
            
            if(callback) {
                callback(data['superclassNames']);
            }
        }
        
        if(ajaxDataCache.hasOwnProperty(className)) {
            exec(ajaxDataCache['classes'][className]); // cache hit
        } else {
            APP.api.getClassDataByClassName(className, function(data) { // cache miss - async call
                ajaxDataCache['classes'][className] = data;
                exec(data);
            });
        }
    }
    
    
            
    // main method start - grab the root class
    // _getD3ClassDataByName("owl:Thing", true);
    _getD3ClassDataByName('<wordnet_capital_108518505>');
    // _getD3ClassToEntityMappingByName("<wikicategory_Federal_countries>");
    
});
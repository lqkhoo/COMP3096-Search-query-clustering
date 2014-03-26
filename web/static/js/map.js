/**
 * @author Li Quan Khoo
 */
var APP = APP || {};

(function($, _, Backbone, d3) {

    // Require
    if (! $)        throw error('jQuery not found.');
    if (! _)        throw error('Underscore.js not found.');
    if (! Backbone) throw error('Backbone.js not found.');
    if (! d3)       throw error('d3 not found');
    
    var DEBUG,
        data,
        ajax,
        Controller,
        Search;
    
    
    DEBUG = true;
    data = {};
    
    ajax = {
        
        /**
         * @param args {PlainObject} arguments
         * 
         * args.searchString {String} raw search string
         */
        postSearchString: function(args) {
            $.ajax({
                type : 'GET',
                url : '/api/entities',
                data : args,
                dataType : 'json',
                success : function(data, textStatus, jqXHR) {
                    APP.controller.setNewSearch(data);
                    APP.controller.render();
                    if (APP.DEBUG) {
                        console.log(data);
                        console.log('Cluster.ajax.postSearchString AJAX success');
                    }
                },
                error : function(jqXHr, textStatus, errorThrown) {
                    if (APP.DEBUG) {
                        console.log('Cluster.ajax.postSearchString AJAX error');
                        console.log(jqXHr);
                        console.log(textStatus);
                        console.log(errorThrown);
                    }
                }
            });
        }
    };
    
    test = function() {
        console.log('test function fired');
    };
    
    Search = Backbone.Model.extend({
        defaults: {
            rawSearchString: undefined,
            validSearchStrings: undefined,
            similarities: undefined,
            entities: undefined
        },
        getSimilarities: function() {
            return this.get('similarities');
        },
        getEntities: function() {
            return this.get('entities');
        }
    });
    
    Controller = Backbone.Model.extend({
        defaults: {
            search: undefined
        },
        getSearch: function() {
            return this.get('search');
        },
        setNewSearch: function(data) {
            this.set('search', new Search(data));
        },
        render: function() {
            
            var width = $(window).width();
            var height = $(window).height();
            var centerX = width / 2;
            var centerY = height / 2;
            
            var entities = this.getSearch().getEntities();
            var similarities = this.getSearch().getSimilarities();
            var entity;
            var similarity;
            var entityNameToIndexHash = {};
            var dataObj = {
                nodes: [],
                links: []
            };
            
            var index = 0;
            var group = 1; // numeric group for use in d3 rendering
            var i;
            
            var groupCount = 0;
            for(var searchString in entities) {
                if(entities.hasOwnProperty(searchString)) {
                    groupCount++;
                }
            }
            groupCount;
            var radius = Math.min(width, height) / 3;
            var anglePerNodeInDeg = 360 / groupCount;
            
            for(var searchString in entities) {
                if(entities.hasOwnProperty(searchString)) {
                    
                    // set central node for searchString
                    if(groupCount === 1) {
                        dataObj.nodes.push({
                            group: 0,
                            name: searchString,
                            fixed: true,
                            root: true,
                            x: centerX,
                            y: centerY
                        });
                    } else {
                        dataObj.nodes.push({
                            group: 0,
                            name: searchString,
                            fixed: true,
                            x: centerX + radius * Math.sin(group * anglePerNodeInDeg * Math.PI / 180),
                            y: centerY + radius * Math.cos(group * anglePerNodeInDeg * Math.PI / 180)
                        });
                    }
                    
                    entityNameToIndexHash[searchString] = index;
                    index++;
                    
                    for(i = 0; i < entities[searchString].length; i++) {
                        
                        // set node
                        entity = entities[searchString][i];
                        dataObj.nodes.push(entity);
                        entityNameToIndexHash[entity['name']] = index; 
                        dataObj.nodes[index]['group'] = group;
                        
                        // set link
                        dataObj.links.push({
                            source: entityNameToIndexHash[searchString],
                            target: entityNameToIndexHash[entity['name']],
                            value: 0.1
                        });
                        
                        index++;
                    }
                }
                group++;
            }
            
            // set cross-cluster links
            index = dataObj.links.length - 1;
            for(i = 0; i < similarities.length; i++) {
                similarity = similarities[i];
                
                dataObj.links.push({
                    source: entityNameToIndexHash[similarity['entity1']],
                    target: entityNameToIndexHash[similarity['entity2']],
                    value: similarity['similarityScore']
                });
            }
            
            console.log(dataObj);
            
            // Force directed graph
            
            var color = d3.scale.category10();
            
            var svg = d3.select('#visualizer-wrapper').append('svg')
                    .attr('width', width)
                    .attr('height', height);
            
            var force = d3.layout.force()
                .nodes(dataObj.nodes)
                .links(dataObj.links)
                .charge(-10000)
                .linkDistance(50)
                .friction(0.3)
                .size([width, height])
                .start();
            
            var link = svg.selectAll('.link')
                .data(dataObj.links)
              .enter().append('line')
                .attr('class', 'link')
                .style('stroke-width', function(d) { return Math.sqrt(d.value); });
            
            var group = svg.selectAll('g.gnode')
                .data(dataObj.nodes)
              .enter().append('g')
                .attr('class', 'gnode')
                .call(force.drag);
            
            var node = group.append('circle')
                .attr('class', 'node')
                .attr('r', 10)
                .style('fill', function(d) { return color(d.group); });
            
            var label = group.append('text')
                .style('font-size', '16px')
                .text(function(d) { return d.name; });
            
            force.on('tick', function() {
                
                link.attr('x1', function(d) { return d.source.x; })
                    .attr('y1', function(d) { return d.source.y; })
                    .attr('x2', function(d) { return d.target.x; })
                    .attr('y2', function(d) { return d.target.y; });
                
                group.attr('transform', function(d) { 
                    return 'translate(' + [d.x, d.y] + ')'; 
                });
                
            });
        }
    });
    
    // Expose API
    APP.DEBUG = DEBUG;
    APP.data = data;
    APP.api = {};
    APP.ajax = ajax;
    APP.api.Controller = Controller;
    
    return APP;
    
}(jQuery, _, Backbone, d3));

$(document).ready(function() {
    
    $('#button-search').click(function() {
        var rawSearchString = $('#input-user-text').val();
        APP.ajax.postSearchString({
            rawSearchString : rawSearchString
        });
    });
    
    APP.controller = new APP.api.Controller();
    
});
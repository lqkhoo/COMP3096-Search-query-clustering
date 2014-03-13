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
            
            console.log(this.getSearch());
            
            var width = $(window).width();
            var height = $(window).height();
            
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
            
            dataObj.nodes.push({
                group: 0,
                name: 'CLUSTER_ROOT_NODE',
                fixed: true,
                x: width / 2,
                y: height / 2
            });
            index++;
            
            for(var searchString in entities) {
                
                // set central node for searchString
                dataObj.nodes.push({
                    group: 0,
                    name: searchString,
                });
                
                entityNameToIndexHash[searchString] = index;
                index++;
                
                // link searchString node to root
                dataObj.links.push({
                    source: 0,
                    target: entityNameToIndexHash[searchString],
                    value: 0.1
                });
                
                if(entities.hasOwnProperty(searchString)) {
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
            
            // Force directed graph with voronoi shape nodes
            // Mostly copied from http://bl.ocks.org/couchand/6420534
            
            var color = d3.scale.category10();
            
            var width = '100%',
                height = '100%';
            var svg = d3.select('#visualizer-wrapper').append('svg')
                .attr('width', width)
                .attr('height', height);
            
            var force = d3.layout.force()
                .nodes(dataObj.nodes)
                .links(dataObj.links)
                .charge(-2000)
                .linkDistance(50)
                .friction(0.3)
                .start();
            
            var link = svg.selectAll(".link")
                .data(dataObj.links)
              .enter().append("line")
                .attr("class", "link")
                .style("stroke-width", function(d) { return Math.sqrt(d.value); });

        var node = svg.selectAll(".node")
            .data(dataObj.nodes)
          .enter().append("circle")
            .attr("class", "node")
            .attr("r", 5)
            .style("fill", function(d) { return color(d.group); })
            .call(force.drag);
        
        force.on("tick", function() {
            
            link.attr("x1", function(d) { return d.source.x; })
                .attr("y1", function(d) { return d.source.y; })
                .attr("x2", function(d) { return d.target.x; })
                .attr("y2", function(d) { return d.target.y; });
            
            node.attr("cx", function(d) { return d.x; })
                .attr("cy", function(d) { return d.y; });
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
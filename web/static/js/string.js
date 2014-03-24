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
                url : '/api/searchmaps',
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
            
            var searchString = this.getSearch().get('searchString');
            var mappings = this.getSearch().get('mappings');
            
            var dataObj = {
                nodes: [],
                links: []
            };
            
            var source = {
                name: searchString,
                fixed: true,
                x: centerX,
                y: centerY
            };
            
            dataObj.nodes.push(source);
            
            targets = [];
            
            for(var string in mappings) {
                if(string !== '') {
                    
                    target = {
                        name: string,
                        cls: 'str',
                        size: mappings[string].length,
                        fixed: false
                    };
                    targets.push(target);

                }
            }
            
            targets.sort(function(a, b) {
                if(a.size < b.size) {
                    return 1;
                }
                if(a.size > b.size) {
                    return -1;
                }
                return 0;
            });
            
            console.log(targets);
            
            var j;
            for(j = 0; j < Math.min(targets.length, 30); j++) {
                dataObj.nodes.push(targets[j]);
                
                dataObj.links.push({
                    source: source,
                    target: targets[j],
                    value: 0.1
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
                .attr('r', function(d) {
                    return 10;
                })
                .style('fill', function(d) { return color(d.group); });
            
            var label = group.append('text')
                .style('font-size', '16px')
                .text(function(d) {
                    if(d.hasOwnProperty('cls')) {
                        return d.name + ' (' + d.size + ')';
                    }
                    return d.name;
                });
            
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
            searchString : rawSearchString
        });
    });
    
    APP.controller = new APP.api.Controller();
    
});
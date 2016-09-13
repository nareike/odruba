var network;
var nodesDataset;
var edgesDataset;
var jsonData;

var lastSelection = {};

var nodesBackup;
var edgesBackup;

var allNodes;
var highlightActive = false;

function httpGet(theUrl) {
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.open("GET", theUrl, false); // false for synchronous request
    xmlHttp.send(null);
    return xmlHttp.responseText;
}

function explain_modal() {
    $("#explain_modal > div").focus();
}

function enter_resource_modal() {
    el = document.getElementById("enter_resource_modal");
    el.style.visibility = (el.style.visibility == "visible") ? "hidden" : "visible";
}

function draw() {
    var url = "vis/graph";
    var rdf = "rdf/graph";
    var jsonString = httpGet(url);
    var rdfString = httpGet(rdf);

    jsonData = JSON.parse(jsonString);
    nodesDataset = new vis.DataSet(jsonData.nodes);
    edgesDataset = new vis.DataSet(jsonData.edges);
    
    /*
    nodesDataset.forEach(function (node) {
        node.hidden = false;
        nodesDataset.update(node);
    });
    */

    nodesBackup = nodesDataset.get({returnType: "Object"});
    edgesBackup = edgesDataset.get({returnType: "Object"});

    var data = {
        nodes: nodesDataset,
        edges: edgesDataset
    };

    var options = jsonData.options;

    /*
     options.layout = {
     hierarchical: true
     }
     */

    options.interaction = {
        selectConnectedEdges: false,
        multiselect: true,
        hover: true,
        keyboard: {
            bindToWindow: false
        }
    };

    options.physics = {
        solver: 'forceAtlas2Based',
        //solver: 'barnesHut',
        //solver: "repulsion",
        stabilization: {
            iterations: 100
        },
        forceAtlas2Based: {
            springLength: 200
        },
        repulsion: {
            springLength: 600
        },
        barnesHut: {
            springLength: 400
        }
    }

    options.edges = {
       width: 2
    };

    options.groups = {
        literal: {
            color: {
                background: '#97C2FC',
                border: '#66A0EF'
            }
        }
    };


    /*
     var options = {
     groups: {
     default: {
     color: {
     background: 'lightgray'
     },
     borderWidth: 2
     }
     },
     nodes: {
     color: {
     border: 'gray',
     background: 'lightgray',
     highlight: {
     border: 'black',
     background: 'white'
     }
     },
     font: {
     color: 'black'
     }
     }
     };
     */

    // create a network
    var container = document.getElementById('mynetwork');
    network = new vis.Network(container, data, options);

    $("#textfeld").val(rdfString);

    network.on("hoverNode", function (e) {
        //neighbourhoodHighlight(e);
    });

    network.on("blurNode", function (e) {
        //neighbourhoodHighlight(e);
    });

    network.on("click", function (e) {
        neighbourhoodHighlight(e);
        disenableButtons(e);
    });

    $.ajax({
        contentType: "application/json",
        url: 'config',
        success: function(result) {
            $("#configData").val(result['graphConfig']);
            $("#inputData").val(result['inputData']);
            $("#ontologyData").val(result['ontologyData']);
            
            $('#rulesets').empty();
            $('.rulepane').remove();
            for(var i = 0; i < result['rulesetData'].length; i++) {
                var rule = result['rulesetData'][i];
                ruleItem(rule.name, rule.data);
            }
        }
    });
    
    $('.fadeMe').css("display", "none");
}

var ruleItem = function(name, data) {
    $('#rulesets').append('<li><a data-toggle="tab" href="#' + name + '-tab">' + name + '</a></li>')
    var tabcontent = '';
    tabcontent += '<div id="' + name + '-tab" class="rulepane tab-pane fade">';
    tabcontent += '    <div class="rulesetname text-info">' + name + '</div>';
    tabcontent += '    <div class="ruleinput form-group">';
    tabcontent += '        <textarea class="form-control" id="' + name + '"></textarea>';
    tabcontent += '    </div>';
    tabcontent += '</div>';
    $('#tabcontentdiv').append($(tabcontent));
    $('#' + name + "").val(data);
}

var disenableButtons = function(e) {
    if (e.edges.length == 1) {
        $('#explainStatement').prop('disabled', false);
    }
    else {
        $('#explainStatement').prop('disabled', true);
    }

    if (e.nodes.length == 1) {
        $('#expandSelected').prop('disabled', false);
        $('#isolateSelected').prop('disabled', false);
        $('#onlySelectedNode').prop('disabled', false);
    }
    else {
        $('#expandSelected').prop('disabled', true);
        $('#isolateSelected').prop('disabled', true);
        $('#onlySelectedNode').prop('disabled', true);
    }
};

var getTriple = function (edge) {
    var edgeData = edgesDataset.get(edge);
    var edgeURI = edgeData.uri;
    var subjectURI = nodesDataset.get(edgeData.from).uri;
    var objectURI = nodesDataset.get(edgeData.to).uri;
    var triple = "<" + subjectURI + "> <" + edgeURI + "> <" + objectURI + "> .";

    return triple;
};

$('document').ready(function () {

    $('#mynetwork').keydown(function (e) {
        switch(e.which) {
            case 27: // esc
                var currentSelection = network.getSelection();
                if (currentSelection.nodes.length == 0 && currentSelection.edges.length == 0) {
                    network.setSelection(lastSelection);
                    neighbourhoodHighlight(lastSelection);
                }
                else {
                    lastSelection.nodes = currentSelection.nodes;
                    lastSelection.edges = currentSelection.edges;
                    network.unselectAll();
                    neighbourhoodHighlight({nodes: [], edges: []});
                }
            break;

            case 32: // space
                $("#stopAnimation").click();
            break;

            case 70: // f
            break;
            
            case 80: // p
                if ($("#expandSelected").is(":enabled")) {
                    $("#expandSelected").click();
                }
            break;

            case 191: // ?
                if (e.shiftKey && $("#explainStatement").is(":enabled")) {
                    $("#explainStatement").click();
                }

                if (!e.shiftKey) {
                    el = $("#quicksearchbar")[0];
                    el.style.visibility = "visible";
                    $("#quicksearch").focus();
                    e.preventDefault();
                }
            break;

            default:
                return;
        }
    })

    $('#explain_modal').keydown(function (e) {
        switch(e.which) {
            case 27: // esc
                explain_modal();
            break;
        }
    })

    $('#textfeld').focus(function () {
        if (this.value == this.title) {
            $(this).val("");
        }
    }).blur(function () {
        if (this.value == "") {
            $(this).val(this.title);
        }
    });

    $('#expand').click(function (e) {
        e.preventDefault();

        $('.fadeMe').css("display", "table-cell");
        $.ajax({
            async: true,
            url: 'vis/expand',
            success: function (result) {
                $('.fadeMe').css("display", "none");
                draw();
            },
            error: function (result) {
                $('.fadeMe').css("display", "none");
            }
        });
    });

    /*
    $('#expandSelected').click(function (e) {
        var edges = network.getSelectedEdges();
        var i;

        var triples = "";
        for (i = 0; i < edges.length; i++) {
            triples += getTriple(edges[i]) + "\n";
        }

        $('.fadeMe').css("display", "table-cell");
        $.ajax({
            type: "POST",
            contentType: "text/turtle",
            url: 'vis/add',
            data: triples,
            success: function (result) {
                $('.fadeMe').css("display", "none");
                draw();
            }
        });
    });
    */

    $('#onlySelectedNode').click(function (e) {
        var nodes = network.getSelectedNodes();
        var uri = nodesDataset.get(nodes[0]).uri;

        $('.fadeMe').css("display", "table-cell");
        $.ajax({
            type: "POST",
            contentType: "text/plain",
            url: 'vis/resource/zoom',
            data: uri,
            success: function (result) {
                $('.fadeMe').css("display", "none");
                draw();
            },
            error: function (result) {
                $('.fadeMe').css("display", "none");
            }
        });
    });

    $('#isolateSelected').click(function (e) {
        var edges = network.getSelectedEdges();
        var i;

        var triples = "";
        for (i = 0; i < edges.length; i++) {
            triples += getTriple(edges[i]) + "\n";
        }

        $('.fadeMe').css("display", "table-cell");
        $.ajax({
            type: "POST",
            contentType: "text/turtle",
            url: 'vis/submit',
            data: triples,
            success: function (result) {
                $('.fadeMe').css("display", "table-cell");
                draw();
            },
            error: function (result) {
                $('.fadeMe').css("display", "table-cell");
            }
        });
    });

    $('#submit').click(function (e) {
        e.preventDefault();

        // information to be sent to the server
        var data = {
            inputData: $('#inputData').val(),
            ontologyData: $('#ontologyData').val(),
            graphConfig: $('#configData').val()
        };
        
        var rulesets = [];
        
        var rules = $('.ruleinput textarea')
        for(var i = 0; i < rules.length; i++) {
            var rule = $(rules[i]);
            var ruleName = rule.attr('id');
            var ruleData = rule.val();
            rulesets.push({name: ruleName, data: ruleData});
        }
        data.rulesetData = rulesets;
        
        $('.fadeMe').css("display", "table-cell");

        $.ajax({
            type: "POST",
            contentType: "application/json",
            url: 'vis/submit',
            data: JSON.stringify(data),
            success: function (result) {
                draw();
                $('.fadeMe').css("display", "none");
            },
            error: function (result) {
                $('.fadeMe').css("display", "none");
            }
        });
    });

    $('#add').click(function (e) {
        e.preventDefault();

        // information to be sent to the server
        var text = $('#textfeld').val();

        $('.fadeMe').css("display", "table-cell");
        $.ajax({
            type: "POST",
            contentType: "text/turtle",
            url: 'vis/add',
            data: text,
            success: function (result) {
                draw();
                $('.fadeMe').css("display", "none");
            },
            error: function (result) {
                $('.fadeMe').css("display", "none");
            }
        });
    });

    $('#reload').click(function (e) {
        $('.fadeMe').css("display", "table-cell");
        $.ajax({
            async: true,
            url: 'vis/reload',
            success: function (result) {
                draw();
                $('.fadeMe').css("display", "none");
            },
            error: function (result) {
                $('.fadeMe').css("display", "none");
            }
        });
    });
    
    $('#stopAnimation').click(function (e) {
        network.stopSimulation();
    });

    $('#explainStatement').click(function (e) {
        var edge = network.getSelectedEdges()[0];
        var triple = getTriple(edge);
        $.ajax({
            type: "POST",
            async: true,
            contentType: 'text/plain',
            data: triple,
            url: 'vis/explain',
            success: function (result) {
                $('#explanation').text(result);
                explain_modal();
            },
            error: function (result) {
                $('.fadeMe').css("display", "none");
            }
        });
    });

    $('#explain_modal').click(function (e) {
        if (e.toElement == $('div#explain_modal')[0]) {
            explain_modal();
        }
    });

    $('#enter_resource_button').click(function (e) {
        var nodes = network.getSelectedNodes();
        if (nodes.length > 0) {
            var uri = nodesDataset.get(nodes[0]).uri;
            $("#enter_resource_input").val(uri);
        }
        //network.stopSimulation();
    });

    $('#enter_resource_input').keydown(function (e) {
        var keypressed = e.keyCode || e.which;
        switch(keypressed) {
            case 13: // enter
                $('#enter_resource_submit').click();
                break;

            case 27: // esc
                $('#enter_resource_modal').modal('hide');
            break;
        }
    });

    $('#enter_resource_submit').click(function (e) {
        var text = $('#enter_resource_input').val();
        $('#enter_resource_modal').modal('hide');
        $('.fadeMe').css("display", "table-cell");

        $.ajax({
            type: "POST",
            contentType: "text/plain",
            url: 'vis/resource/simple',
            data: text,
            success: function (result) {
                draw();
                $('.fadeMe').css("display", "none");
            },
            error: function (result) {
                $('.fadeMe').css("display", "none");
            }
        });
    });

    $('#enter_resource_add').click(function (e) {
        var text = $('#enter_resource_input').val();
        $('#enter_resource_modal').modal('hide');
        $('.fadeMe').css("display", "table-cell");

        $.ajax({
            type: "POST",
            contentType: "text/plain",
            url: 'vis/resource/simple/add',
            data: text,
            success: function (result) {
                draw();
                $('.fadeMe').css("display", "none");
            },
            error: function (result) {
                $('.fadeMe').css("display", "none");
            }
        });
    });

    $('#expandSelected').click(function (e) {
    //$('#tryme').click(function (e) {
        var selnodes = network.getSelectedNodes();

        for(var j = 0; j < selnodes.length; j++) {
            var uri = nodesDataset.get(selnodes[j]).uri;
            var pos = network.getPositions(selnodes)[selnodes[j]];
            var nx = pos.x;
            var ny = pos.y;

            network.unselectAll();
            neighbourhoodHighlight({edges : [], nodes: []});

            $('.fadeMe').css("display", "table-cell");
            $.ajax({
                type: "POST",
                contentType: "text/plain",
                url: 'vis/resource',
                data: uri,
                error: function (result) {
                    $('.fadeMe').css("display", "none");
                },
                success: function (result) {
                    $('.fadeMe').css("display", "none");
                    jsonData = result;
                    //jsonData = JSON.parse(result);
                    var i;
                    
                    for (i = 0; i < jsonData.nodes.length; i++) {
                        var newNode = jsonData.nodes[i];
                        //newNode.x = nx;
                        //newNode.y = ny;
                        try {
                            nodesDataset.add(newNode);
                        }
                        catch (e) {
                        }
                    }

                    for (i = 0; i < jsonData.edges.length; i++) {
                        var newEdge = jsonData.edges[i];
                        try {
                            edgesDataset.add(newEdge);
                        }
                        catch (e) {
                        }
                    }
                    
                    nodesBackup = nodesDataset.get({returnType: "Object"});
                    edgesBackup = edgesDataset.get({returnType: "Object"});

                    $.ajax({
                        contentType: "text/plain",
                        url: 'rdf/graph',
                        success: function(result) {
                            $("#textfeld").val(result);
                        }
                    });
                }
            });
        }
        network.selectNodes(selnodes, false);
    });

    $('#quicksearch').blur(function(e) {
        $("#quicksearchbar")[0].style.visibility = "hidden";
    });

    $('#quicksearch').keydown(function(e) {
        switch(e.which) {
            case 27:
                $("#quicksearchbar")[0].style.visibility = "hidden";
                $("#mynetwork").focus();
            break;

            case 13:
                var searchString = new RegExp($('#quicksearch').val(), "i");
                network.unselectAll();
                neighbourhoodHighlight({edges : [], nodes: []});

                if ($('#quicksearch').val() !== "") {
                    var showNodes = nodesDataset.get({
                        filter: function (node) {
                            return (searchString.test(node.label) || searchString.test(node.title));
                        }
                    });
                    var nodeIDs = showNodes.map(function(a) {return a.id;});

                    var showEdges = edgesDataset.get({
                        filter: function (edge) {
                            return (searchString.test(edge.label));
                        }
                    });
                    var edgeIDs = showEdges.map(function(a) {return a.id;});

                    network.setSelection({nodes: nodeIDs, edges: edgeIDs});
                    disenableButtons({nodes: nodeIDs, edges: edgeIDs});
                    neighbourhoodHighlight({nodes: nodeIDs, edges: edgeIDs});
                 }

            break;
        }
    });

    $('#cluster_outliers').click(function (e) {
        network.clusterOutliers();
    });

});

function neighbourhoodHighlight(params) {
    //neighbourhoodHide(params)
    neighbourhoodGrayout(params);
}

function neighbourhoodHide(params) {
    // if something is selected:
    if (params.nodes.length > 0) {
        highlightActive = true;
        var i, j;

        var hiddenNodes = new vis.DataSet();
        var hiddenEdges = new vis.DataSet();

        nodesDataset.forEach(function (node) {
            node.hidden = true;
            hiddenNodes.add(node);
        })

        edgesDataset.forEach(function (edge) {
            edge.hidden = true;
            hiddenEdges.add(edge);
        })


        for (j = 0; j < params.nodes.length; j++) {
            var selectedNode = params.nodes[j];
            var connectedNodes = network.getConnectedNodes(selectedNode);
            for (i = 0; i < connectedNodes.length; i++) {
                var node = nodesDataset.get(connectedNodes[i]);
                node.hidden = false;
                hiddenNodes.update(node);
            }

            var selNode = nodesDataset.get(selectedNode);
            selNode.hidden = false;
            hiddenNodes.update(selNode)

            var showEdges = edgesDataset.get({
                filter: function (edge) {
                    return (edge.to == selectedNode || edge.from == selectedNode);
                }
            });

            showEdges.forEach(function (edge) {
                edge.hidden = false;
                hiddenEdges.update(edge);
            });
        }

    }
    else {
        // reset all nodes
        var hiddenNodes = new vis.DataSet();
        nodesDataset.forEach(function (node) {
            node.hidden = false;
            hiddenNodes.add(node);
        });

        var hiddenEdges = new vis.DataSet();
        edgesDataset.forEach(function (edge) {
            edge.hidden = false;
            hiddenEdges.add(edge);
        });
        
        if (params.edges.length > 0) {
            for (j = 0; j < params.edges.length; j++) {
                var selectedEdge = params.edges[j];
                var edge = edgesDataset.get(selectedEdge);
                hiddenEdges.update(edge);
            }
        }

        highlightActive = false
    }

    var updateArrayNodes = [];
    var updateArrayEdges = [];

    hiddenNodes.forEach(function (node) {
        updateArrayNodes.push(node);
    });
    hiddenEdges.forEach(function (edge) {
        updateArrayEdges.push(edge);
    });

    nodesDataset.update(updateArrayNodes);
    edgesDataset.update(updateArrayEdges);

}

function neighbourhoodGrayout(params) {
    // if something is selected:
    if (params.nodes.length > 0) {
        highlightActive = true;
        var i, j;
        
        var lightGray = "rgba(220, 220, 220, 0.5)";
        var darkGray = "rgba(200, 200, 200, 0.5)";

        var hiddenNodes = new vis.DataSet();
        var hiddenEdges = new vis.DataSet();

        // first hide/gray out all nodes
        nodesDataset.forEach(function (node) {
            node.color = {
                background: lightGray,
                border: darkGray
            };
            node.shadow = false;
            node.font = {
                color: darkGray
            };
            if (node.icon != undefined) {
                // don't change the icon object but use a new one!
                node.icon = {
                    code: node.icon["code"],
                    color: darkGray
                }
            }
            hiddenNodes.add(node);
        });

        // also hide all edges
        edgesDataset.forEach(function (edge) {
            edge.color = lightGray;
            edge.font = {
                color: darkGray
            };
            hiddenEdges.add(edge);
        });

        // for each selected nodes
        for (j = 0; j < params.nodes.length; j++) {
            var selectedNode = params.nodes[j];
            // ... get all connected nodes and remove them from the dataset of nodes to be hidden
            var connectedNodes = network.getConnectedNodes(selectedNode);
            for (i = 0; i < connectedNodes.length; i++) {
                var node = nodesDataset.get(connectedNodes[i]);
                hiddenNodes.remove(node);
                hiddenNodes.add(nodesBackup[connectedNodes[i]]);
            }

            // hide the selected node itself
            var selNode = nodesDataset.get(selectedNode);
            hiddenNodes.remove(selNode);
            hiddenNodes.add(nodesBackup[selectedNode]);

            // remove outgoing or incoming edges for the selected node from the dataset of nodes to be hidden
            var showEdges = edgesDataset.get({
                filter: function (edge) {
                    return (edge.to == selectedNode || edge.from == selectedNode);
                }
            });

            showEdges.forEach(function (edge) {
                hiddenEdges.remove(edge);
                hiddenEdges.add(edgesBackup[edge.id]);
            });

            for (k = 0; k < params.edges.length; k++) {
                var edgeID = params.edges[k];
                var edge = edgesDataset.get(edgeID);
                hiddenEdges.remove(edge);
                hiddenEdges.add(edgesBackup[edge.id]);
            }
        }

    }
    else {
        var i;
        // reset all nodes
        var hiddenNodes = new vis.DataSet();
        for(i in nodesBackup) {
            if (nodesBackup.hasOwnProperty(i)) {
                hiddenNodes.add(nodesBackup[i]);
            }
        }

        var hiddenEdges = new vis.DataSet();
        for(i in edgesBackup) {
            if (edgesBackup.hasOwnProperty(i)) {
                hiddenEdges.add(edgesBackup[i]);
            }
        }

        if (params.edges.length > 0) {
            for (j = 0; j < params.edges.length; j++) {
                var selectedEdge = params.edges[j];
                var edge = edgesDataset.get(selectedEdge);
                hiddenEdges.update(edge);
            }
        }

        highlightActive = false
    }

    var updateArrayNodes = [];
    var updateArrayEdges = [];

    hiddenNodes.forEach(function (node) {
        if (nodesBackup.hasOwnProperty(node.id)) {
            for(var p in nodesDataset.get(node.id)) {
                var n = nodesDataset.get(node.id);
                if (n.hasOwnProperty(p) && !node.hasOwnProperty(p)) {
                    node[p] = null;
                }
            }
        }
        updateArrayNodes.push(node);
    });
    hiddenEdges.forEach(function (edge) {
        for(var p in edgesDataset.get(edge.id)) {
            var e = edgesDataset.get(edge.id);
            if (e.hasOwnProperty(p) && !edge.hasOwnProperty(p)) {
                edge[p] = null;
            }
        }
        updateArrayEdges.push(edge);
    });

    nodesDataset.update(updateArrayNodes);
    edgesDataset.update(updateArrayEdges);

}

var isResizing = false,
    lastDownX = 0;
var lastWidth = null;

$(function () {
    var container = $('#wrapper'),
        left = $('#wrapper-left'),
        right = $('#wrapper-right'),
        handle = $('#drag');

    handle.dblclick(function() {
        if (left.width()+1 >= container.width() && lastWidth != null) {
            left.css('width', lastWidth);
            right.css('width', container.width() - lastWidth);
        }
        else {
            lastWidth = left.width();
            left.css('width', '100%');
            right.css('width', '0');
        }
    });

    handle.on('mousedown', function (e) {
        e.preventDefault();
        isResizing = true;
        lastDownX = e.clientX;
    });

    $(document).on('mousemove', function (e) {
        // we don't want to do anything if we aren't resizing.
        if (!isResizing)
            return;

        var offsetRight = container.width() - (e.clientX - container.offset().left);

        var position = e.clientX - container.offset().left;
        var percentage = 100 * position / container.width();

        //left.css('right', offsetRight);
        //right.css('width', offsetRight);
        left.css('width', percentage + '%');
        right.css('width', (100 - percentage) + '%');
    }).on('mouseup', function (e) {
        // stop resizing
        isResizing = false;
    });
});


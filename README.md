# Odruba - Ontology-driven Rule-based Visualisation

Odruba calculates a visual representation of an RDF graph. The RDF graph gets annotated with styling information using additional triples. Styling rules are formalized using Jena Rules. In order to derive the desired style, RDFS/OWL semantics and external ontologies can be incorporated. The annotated RDF graph will then be translated into a JSON graph description, that will then displayed by [vis.js](http://visjs.org/). The resulting graph can be filtered and expanded.

> _Remark:_ This documentation is work in progress and will soon expand. Right now it should give you a quick overview.

> _Remark:_ Also be aware that Odruba is work in progress and only in early alpha stage.

## Getting started

Clone the repo, change into the `odruba` folder and execute

```
mvn spring-boot:run
```

This will fetch the required packages and start the application. Open your browser and navigate to

```
http://localhost:8080
```

You should see Odruba a simple graph.

> _Remark:_ For a different port, you can use
>
> ```
> mvn spring-boot:run -Dserver.port=<port>
> ```
>
> where `<port>` is the desired port.

To learn about Odruba we prepared some demo data to explore some features.

## Odruba Demo Data

For a quick demonstration, there are some Turtle files included in the `resources/ontologies` folder. They are mainly exported from the [LinkedMDB](http://www.linkedmdb.org/), but do not comprise the total LinkedMDB (in total, the sub-graph included in Odruba contains 5646 triples)

For the demo, we will manage a group of moviegoers that watch certain films and also have some preferences regarding genres.

## Quick GUI Explanation

### Left-hand side

The Odruba GUI is divided into two (resizable) panes. In the left pane, the visual graph representation is displayed (let's call this the 'graph window'). Nodes can be dragged around and screen. When you hover over a node (or edge), a small pop-up will show the URI and some information. Also you can zoom the graph window with the scroll wheel and pan it by dragging it around with the mouse. Alternatively, you can use the +/- and arrow keys.

Nodes and edges can be clicked to select them. Clicking while holding the `Ctrl` key will add the clicked element to the active selection or remove it from the selection if it was selected before. For each selected node, its direct neighbors will also be highlighted while the other nodes are greyed out.

Make sure the graph window is focused (there is a blue border around it in that case). To quickly find a certain node, you can press `/` and bring up a quick search bar on the bottom of the graph window. Enter you search bar and press `Enter`. The nodes matching the search term will be highlighted, while those not matching will be greyed out. In case no node matches, all nodes remain their normal visibility. The `Escape` key closed the quick search. Press `Escape` again to clear the selection.

> _Remark:_ The search term can actually be a (Javascript) regular expression.

> _Hand-on:_ Right now our graph is relatively boring. Let's explore it a bit. Highlight the node "Bonnie and Cly..." and press the `Expand node` button below the graph window. This will fetch some more data from the background knowledge, in this case the LinkedMDB.

Which triples will be included and displayed in the graph? This depends on the configuration of Odruba (see below) and is use-case specific. You might not be interested in the directors or actors of a movie, so you could choose not to include them when using the `Expand node` button (alternatively, press `p` on your keyboard). This is the reason why trying to expand an actor (the yellow star icon) will not produce any results in this demo.

As you expand the node labeled "Arthur Penn" you will notice that the new nodes lack any styling and also there is no movie title but only a CURIE. The reason for this is that there are no triples describing e.g. `film:2091` in the sub-graph exported from the LinkedMDB.

The style of the movies node (a purple ellipse) is infered from the `rdf:type` of a node. But surely, something that has a director, is a movie. In the next section we will see how to teach Odruba to color those nodes as well.

One important button below the graph window is the red `Reload` button. This button will bring you can to the state as if you just started Odruba from the terminal.

### Right-hand side

In the right pane you can see the configuration which is sub-divided into different tabs. The data you see here are directly loaded from disk. You can modify it and `Submit`, meaning to reload the graph with the new configuration. However, right now, the changed configuration will not be saved to disk. If you mess up the configuration, you can use the red `Reload` button to reload the configuration as well as re-render the graph.

#### Tab "Graph Data"

At first, have a look at the `Graph Data` tab. It should be activated when you first start Odruba. The triples here are the graph that should be displayed. If you compare the demo graph with the actual graph, you will notice that some triples are 'missing'. By default, Odruba does not display classes or literals, as they tend to 'clutter' the graph with visual noise. Of course, this can be changed.

#### Tab "Ontology Data"

Next, have a look at the `Ontology Data` tab. Data here will not be automatically displayed but constitues background knowledge. Nevertheless, it can influence how the styling information is applied. It's probably not desirable to fill the `Ontology Data` window with thousands of triples. That's why big ontologies are imported via on `owl:imports` statement.

> _Hand-on:_ Earlier, we noticed that most of the nodes connected to the director Arthur Penn do not have any styling associated. We can fix this now by adding the information, that everything that has a director is indeed a movie. In the `Ontology Data` window, add this statement:
>
> ```
> movie:director rdfs:domain movie:film .
> ```
>
> Hit the `Submit` button. The graph will be reloaded. Again, expand the node "Bonnie and Cly..." and then expand "Arthur Penn". The films without title are now styled in the same way than "Bonnie and Cly...".

#### Tab "Rulesets"

The `Rulesets` tab holds a number of rule sets that are executed from top to bottom. Each rule set contains a number of Jena Rules which follow the [Jena Rules Syntax](https://jena.apache.org/documentation/inference/#RULEsyntax). Logically, Jena Rules are similar to Horn clauses with some additional [builtin primitives](https://jena.apache.org/documentation/inference/#RULEbuiltins).

Odruba uses rules to determine styling for nodes. Click on the `Rulesets` tab and choose 'styling' to open the styling rules for the demo. The rules use properties that are derived from the [vis.js nodes properties](http://visjs.org/docs/network/nodes.html). Let's experiment with this:

> _Hands-on:_ Open the 'styling' ruleset. In the first rule named "colorMoviegoer" there are two conclusion, one of which is commented-out by a hash symbol (`#`). Delete to hash and `Submit` the changes. The moviegoer nodes should now appear as a [Fontawesome user icon](http://fontawesome.io/icon/user/).

There are basically two types of rules for Odruba. On the one hand, rules can directly influence the styling of a node. On the other hand, there are rules that produce triple that are no not represent styling information. Triples that are produced that way can appear in a visualisation. And explanation can also be displayed.

> _Hands-on:_ Open the 'example' ruleset. There is one rules title "like-genre-like-film". It's commented out by leading hashed (`#`). Delete all hashes and submit the changes. You should now see two arrows labeled `ex:likes` from Alice to "Bonnie and Cly..." as well as to "Pulp Fiction".
>
> Why does Alice like those movies? Select on one of the arrows/edges and then click the `Explain` button below the graph window. It turns out that Alice like `genre:4` and those movies belong to this genre. The genre preference was in fact included from the moviegoers ontology.

## Configuration

Odruba can be configured either via an HTTP request or by providing configuration files at startup. While the frontend uses the the first method, we will only take a closer look at the configuration files.

The main configuration file is the file `application.yaml`. You can also use a different file by starting your application with another Spring profile by using

```
mvn spring-boot:run -Drun.profiles=[profilename]
```

to use the yaml file `application-[profilename].yaml`.

Among other Spring-specific configuration options, there are the files listed that will be loaded into the configuration tabs in the GUI.

```
odruba:
  graph: src/main/resources/config/graph_config.ttl
  data:
    input: src/main/resources/tutdata.ttl
    ontology: src/main/resources/config/ontology_config.ttl
    rulesets:
      - src/main/resources/rulesets/example.rules
      - src/main/resources/rulesets/styling.rules
```

## Architecture overview

Odruba uses client-server architecture. The back end will

- manage a triplestore (background knowledge)
- provide reasoning capabilities
    - regarding RDFS/OWL semantics
    - with respect to rules
- annotated an RDF graph with visualisation information
- convert an annotated RDF graph to a JSON that can be displayed by vis.js
- provide a HTTP-API that
    - accepts configuration files
    - accepts requests for JSON for a specific (sub) graph
    - serve JSON to the JavaScript front end

The front end will

- display a visual force-directed graph
- display the RDF that should be rendered
- display the background knowledge as Turtle
- display the rules sets
- provide tools to
    - highlight/find nodes
    - exlucde nodes
    - fetch new information from the backend


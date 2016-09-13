# Frequently Asked Questions

## Why?

RDF graphs usually are not only self-contained data structures. Visualising _just_ the information explicitly encoded in a given RDF graph yields unsatisfactory results. There are formalized semantics (RDF, RDFS, OWL) that help dealing with the data.

Ontologies define specific terms using these semantics. For instance, the [FOAF Vocabulary](http://xmlns.com/foaf/spec/) defines terms relating to social relations between people. By taking into consideration the FOAF ontology, the triple `<Alice> foaf:knows <Bob> .` implies, that `<Alice>` and `<Bob>` are both persons (i.e. of type `foaf:Person`).

This kind of background knowledge should not be included directly into graph (i.e. as a triple `<Alice> rdf:type foaf:Person`), but could still be considered for styling (i.e. Alice could be rendered as an [user icon](http://fontawesome.io/icon/user/). By using a reasoner to apply styling rules, Odruba can leverage this background knowledge.

## What's the use-case?

Typically, Odruba aims to visualize small to medium sized RDF graphs. One use case is to select a small sub-graph of a larger knowledge base, i.e. all movies of a certain director out movie database.

Visualisation libraries on modern computers can display force-directed graphs with some 1000 nodes (though [limitation apply](https://en.wikipedia.org/wiki/Force-directed_graph_drawing#Disadvantages)). However, Odruba does not primarily focus on visualising graphs of that size.

## Is Odruba only for visualisation?

Not exacly. Since Odruba uses a reasoner and rules, an 'extended' use case is to define certain domain specific rules that might result in a specific color to be applied to a node. The rule-based approach is flexible enough to indicate missing or critical information visually.

## Can I use Odruba headless (i.e. without frontend GUI)?

Yes, and no. Odruba employs a client-server architecture to connect the Java backend with the Javascript front-end. It can be useful to fetch certain information via an HTTP request. However, the Odruba-API will change somewhat to support multitenancy. Expect a better documented API description in the near future.

## Is Orduba production ready?

No, Odruba is in early alpha stage. Not too much work has been put into usability,  deployment or interaction with an existing triplestore.

## Why can't I use SPARQL?

Right now, Odruba is in an early alpha-stage (see previous question). However, the Jena libraries provides components that enable SPARQL queries quite easily so you can defininitely except this to be included.


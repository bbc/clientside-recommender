clientside-recommender
=================

A client-side recommender system implemented in Javascript. It uses the Weighted Item kNN algorithm, as implemented in the MyMediaLite Recommender System Framework which can be used to train the required kNN model.

* [http://mymedialite.net/](http://mymedialite.net/)

For the theoretical backgound see "Item-based nearest neighbor recommendation" in:

    Recommender Systems: An Introduction
    Dietmar Jannach, Markus Zanker, Alexander Felfernig, Gerhard Friedrich
    ISBN: 9780521493369
    Pages: 18 - 20

An example application is available here:

* [http://sibyl.prototyping.bbc.co.uk/](http://sibyl.prototyping.bbc.co.uk/)

To use the engine you must to load two JavaScript files - one containing the engine, the other containing the recommender model.

Recommender engine
--------------------

The engine implements pre-filtering, prediction, ranking and post-filtering.

* [engine/receng-wknn-1.0.js](https://github.com/bbcrd/clientside-recommender/blob/master/engine/receng-wknn-1.0.js)
    
It provides two methods:

1) Get an ordered list of all items

    Receng.getChoices(genre, type, likes, dislikes)

You set the order of this list when you build the model. For example it could be in order of popularity (i.e. non-personalised recommendations)

2) Get an ordered list of recommendations based on the user's likes and dislikes

    Receng.getRecs(genre, type, likes, dislikes)

Parameters for both methods:

*type* is "all" (or null or undefined) or a string representing the required media type e.g. "0" for audio or "1" for video.

*genre* is "all" (or null or undefined) or a string representing the required content genre e.g. "0" for comedy or "1" for drama etc.

*likes* and *dislikes* are arrays of item IDs which the user likes or dislikes. If null or undefined they are ignored. In the getChoices method these IDs are simply blacklists (they remove the items from the results). In the getRecs method they are used to personalize the results - if no likes or dislikes are provided getRecs returns the same list as getChoices.
    
Both methods return an indexed array of programmes (the best recommendation is at index 0). Each entry is an associative array with the following key/value pairs:
    
<table> 
<tr><th>Key</th><th>Value</th></tr>
<tr><td>�p�</td><td>item D</td></tr>
<tr><td>�t�</td><td>title</td></tr>
<tr><td>�g�</td><td>genre</td></tr>
<tr><td>�v�</td><td>media type</td></tr>
<tr><td>"x"</td><td>expiry date (optional) e.g. "2012/12/27 20:55:00"</td></tr> 
</table>

Items beyond their expiry date (if present) will not be returned.

The engine includes a genre diversification post-filter which prevents the same genre appearing more than once in three consecutive recommendations. To disable set the diversify variable in the getRecs prototype to false.

Recommender model 
-------------------

The model file contains a kNN model and some basic item metadata which is used for filtering and presentation purposes. The metadata consists of title, genre, media type and an optional expiry data but additional fields can be added as required.  For an example model file see: 

* [engine/example_model.js](https://github.com/bbcrd/clientside-recommender/blob/master/engine/example-model.js)

The models can either use content-based filtering (based on item-attributes) or use collaborative filtering (based on user-item feedback). Two example files show how to build these models:
 
* [src/AttributeBasedModelBuilder.java](https://github.com/bbcrd/clientside-recommender/blob/master/src/AttributeBasedModelBuilder.java)
* [src/CollaborativeFilteringModelBuilder.java](https://github.com/bbcrd/clientside-recommender/blob/master/src/CollaborativeFilteringModelBuilder.java)

Note that these files have several TODO comments where you need to supply data of some kind. Both require this JAR file in the classpath:

* [lib/mymedialite.jar](https://github.com/bbcrd/clientside-recommender/tree/master/lib)
    
Licensing terms and authorship
------------------------------

See [COPYING](COPYING)

## Authors

See [AUTHORS](AUTHORS)

## Acknowledgements

This work was co-funded through the European Commission FP7 project ViSTV-TV under grant agreement No. 269126.

## Copyright

Copyright 2013 British Broadcasting Corporation

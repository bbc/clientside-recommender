function Receng() {
  var method = "WeightedItemKNN";
  var diversify = true;
}

Receng.prototype.getChoices = function(genre, type, likes, dislikes) {
  if(!genre) genre = "all";
  if(!type) type = "all";
  if(!likes) likes = [];
  if(!dislikes) dislikes = [];
  
  // Filter available items 
  var selection = new Array();
  var date = new Date();
  for(var i in Receng.items) {
    var item = Receng.items[i];

    // Omit expired items
    var expires = item['x'];
    if(expires != null) {
      var expiryDate = new Date(expires);
      if(expiryDate < date) continue;
    }
    
    if(genre == "all" || genre == item['g']) {
      if(type == "all" || type == item['v']) { 
        var pid = item['p'];
        if($.inArray(pid, likes) == -1 && $.inArray(pid, dislikes) == -1) selection.push(item);
      }
    }
  }
  return selection;
};

Receng.prototype.getRecs = function(genre, type, likes, dislikes) {
  if(!genre) genre = "all";
  if(!type) type = "all";
  if(!likes) likes = [];
  if(!dislikes) dislikes = [];
  
  // Filter candidate items and predict weights
  var weights = new Array();
  var date = new Date();
  for(i in Receng.items) {
    var item = Receng.items[i];
    var pid = item['p'];
    
    // Omit items in likes and dislikes
    if($.inArray(pid, likes) != -1 || $.inArray(pid, dislikes) != -1) continue;
      
    // Omit expired items
    var expires = item['x'];
    if(expires != null) {
      var expiryDate = new Date(expires);
      if(expiryDate < date) continue;
    }
    
    // Filter by genre and type
    if(genre == "all" || genre == item['g']) {
      if(type == "all" || type == item['v']) { 
        var weight = this.predict(i, likes, dislikes);
        weights.push({n:i, w:weight});
      }
    }
  }
  
  // Rank items
  weights.sort(this.inverseSortWeight);
  var results = new Array();
  
  if(genre != "all" || diversify == false) {
    for(var i=0; i < weights.length; i++) results.push(Receng.items[weights[i]['n']]);
  } else {
    // Diversify genre
    var lastGenre1 = "-";
    var lastGenre2 = "-";
    for(var i = 0; i < weights.length; i++) {
      var index = 0;
      while(index < weights.length) {
        var item = Receng.items[weights[index]['n']];
        var itemGenre = item['g'];
        if(itemGenre != lastGenre1 && itemGenre != lastGenre2) {
          results.push(item);
          weights.splice(index, 1);
          lastGenre2 = lastGenre1;
          lastGenre1 = itemGenre;
          break;
        }
        index++;
      }
    }
  }
  return results;
};

Receng.prototype.inverseSortWeight = function(a, b) {
  return b['w'] - a['w'];
};

Receng.prototype.predict = function(itemId, likes, dislikes) {
  var weight = 0.0;
  var neighbors = Receng.nn[itemId];
  var weights = Receng.weights[itemId]; 
  for(var i in neighbors) {
    var pid = Receng.items[neighbors[i]]['p'];
    if($.inArray(pid, likes)    != -1) weight += weights[i];
    if($.inArray(pid, dislikes) != -1) weight -= weights[i];
  }
  return weight;
};

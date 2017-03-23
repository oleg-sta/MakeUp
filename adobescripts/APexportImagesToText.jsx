#target photoshop  
// exporting layers and sublayers as png files

var doc = app.activeDocument;

var saveFile = File("h:/AndroidProjects/flightlabs/makeup/fromdesigner20170319/links/layerssadow/images.txt");
if(saveFile.exists)
    saveFile.remove();
saveFile.encoding = "UTF8";
saveFile.open("w");

for(var i = 0 ; i < doc.layers.length;i++) {
    var line = doc.layers[i].name;
    var eyeline = "";
    var eyeshadow = "";
    var eyelashes = "";
    var lay = doc.layers[i];
    if (lay.layers != null) {
        for(var j = 0 ; j < lay.layers.length;j++) {
//        line += ";" + lay.layers[j].name;
          if (lay.layers[j].name.indexOf("lash") != -1) {
              eyelashes = lay.layers[j].name;
          }
          if (lay.layers[j].name.indexOf("line") != -1) {
              eyeline = lay.layers[j].name;
          }
          if (lay.layers[j].name.indexOf("shad") != -1) {
              eyeshadow = lay.layers[j].name;
          }
        }
    }
    saveFile.writeln(line + ";" + eyeline + ";" + eyelashes + ";" + eyeshadow);
}

saveFile.close();
saveFile.close();
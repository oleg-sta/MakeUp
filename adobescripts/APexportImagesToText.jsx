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
    var tail = "";
    var colorLips = "";
    if (lay.layers != null) {
        for(var j = 0 ; j < lay.layers.length;j++) {
//        line += ";" + lay.layers[j].name;
          if (lay.layers[j].name.indexOf("lash") != -1) {
              eyelashes = lay.layers[j].name;
          } else if (lay.layers[j].name.indexOf("line") != -1) {
              eyeline = lay.layers[j].name;
          } else if (lay.layers[j].name.indexOf("shad") != -1) {
              eyeshadow = lay.layers[j].name;
          } else if (lay.layers[j].kind == "LayerKind.SOLIDFILL" && lay.layers[j].name.indexOf("ips") > 0) {
              //var color = lay.layers[j].getObjectValue(charIDToTypeID('Clr ')); 
              app.activeDocument.activeLayer = lay.layers[j];
              var col = getFillColor();
              colorLips = col.rgb.hexValue;
          } else {
              tail += ";" + lay.layers[j].name + "(" + lay.layers[j].kind + ")";
          }
        }
    }
    saveFile.writeln(line + ";" + eyeline + ";" + eyelashes + ";" + eyeshadow + ";" + colorLips + tail);
}

saveFile.close();
saveFile.close();


function getFillColor(){
   var ref = new ActionReference();
   ref.putEnumerated( stringIDToTypeID( "contentLayer" ), charIDToTypeID( "Ordn" ), charIDToTypeID( "Trgt" ));
   var ref1= executeActionGet( ref );
   var list =  ref1.getList( charIDToTypeID( "Adjs" ) ) ;
   var solidColorLayer = list.getObjectValue(0);        
   var color = solidColorLayer.getObjectValue(charIDToTypeID('Clr ')); 
   var fillcolor = new SolidColor;
   fillcolor.rgb.red = color.getDouble(charIDToTypeID('Rd  '));
   fillcolor.rgb.green = color.getDouble(charIDToTypeID('Grn '));
   fillcolor.rgb.blue = color.getDouble(charIDToTypeID('Bl  '));
   return fillcolor;
}
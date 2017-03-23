// this script for exporting specific group to png files

#target photoshop  
function main(){  
if(!documents.length) return;  
var doc = activeDocument;  
var oldPath = activeDocument.path;  
for(var a=0;a<doc.layers.length;a++){  
activeDocument.activeLayer = activeDocument.layers.getByName(doc.layers[a].name);  
//dupLayers();  
var saveFile= File("h:/Garbage/3/"+doc.layers[a].name +".png");  
SavePNG(saveFile);  
//app.activeDocument.close(SaveOptions.DONOTSAVECHANGES);  
for (var a2 = 0; a2 < activeDocument.layers.getByName(doc.layers[a].name).groupItems.length; a2++)
{
   activeDocument.layers.getByName(doc.layers[a].name).groupItems[a2].hidden=true;
}
for (var a2 = 0; a2 < activeDocument.layers.getByName(doc.layers[a].name).pageItems.length; a2++)
{
   activeDocument.layers.getByName(doc.layers[a].name).pageItems[a2].hidden=true;
}
for (var a2 = 0; a2 < activeDocument.layers.getByName(doc.layers[a].name).groupItems.length; a2++)
{
   //activeDocument.layers.getByName(doc.layers[a].name).groupItems[a2].name=activeDocument.layers.getByName(doc.layers[a].name).groupItems[a2].name + "1455";
   activeDocument.layers.getByName(doc.layers[a].name).groupItems[a2].hidden=false;
   var gr = activeDocument.layers.getByName(doc.layers[a].name).groupItems[a2];
   

//   $.writeln ("creating new doc!"); 
//CallFunction ("0" + gr.pageItems.length);
var fileName = saveFile + "aa" + a2 + ".png";
for (var a3 = 0; a3 < gr.pageItems.length; a3++)
{
 if (typeof(gr.pageItems[a3]) != "undefined" && gr.pageItems[a3].pageItems != null) {
 var ee = gr.pageItems[a3].pageItems;
  for (var a4 = 0; a4 < gr.pageItems[a3].pageItems.length; a4++)
  {
    
//    CallFunction ("yeeee!!!e " + gr.pageItems[a3].pageItems[a4].typename);
    if (gr.pageItems[a3].pageItems[a4].typename == "PlacedItem") {
//        CallFunction ("yeeee!!!e " + gr.pageItems[a3].pageItems[a4].file.name);
        fileName = "h:/Garbage/3/" + gr.pageItems[a3].pageItems[a4].file.name + ".png";
    }
  }
 }
 try{
 if (gr.pageItems[a3] != null && gr.pageItems[a3].typename == "PlacedItem" && gr.pageItems[a3].file != null) {
        fileName = "h:/Garbage/3/" + gr.pageItems[a3].file.name + ".png";
 }
 } catch(e) {}
}


   SavePNG(fileName);  
   activeDocument.layers.getByName(doc.layers[a].name).groupItems[a2].hidden=true;
}


    }  
}  
main();  
function dupLayers() {   
    var desc143 = new ActionDescriptor();  
        var ref73 = new ActionReference();  
        ref73.putClass( charIDToTypeID('Dcmn') );  
    desc143.putReference( charIDToTypeID('null'), ref73 );  
    desc143.putString( charIDToTypeID('Nm  '), activeDocument.activeLayer.name );  
        var ref74 = new ActionReference();  
        ref74.putEnumerated( charIDToTypeID('Lyr '), charIDToTypeID('Ordn'), charIDToTypeID('Trgt') );  
    desc143.putReference( charIDToTypeID('Usng'), ref74 );  
    executeAction( charIDToTypeID('Mk  '), desc143, DialogModes.NO );  
};  
function SavePNG(saveFile){  
    var exportOptions = new ExportOptionsPNG24();
    var type = ExportType.PNG24;
    var fileSpec = new File(saveFile);
//    exportOptions.colorCount = 8;
    exportOptions.transparency = true;
    exportOptions.verticalScale = 400;
    exportOptions.horizontalScale = 400;
    app.activeDocument.exportFile( fileSpec, type, exportOptions );
} 

function CallFunction (Call) {  
    win = new Window ("dialog", Call);  
    win.add ("statictext", undefined, Call);  
    win.size = [800,800];
    win.show();  
    $.sleep(150);  
    win.close();  
} 
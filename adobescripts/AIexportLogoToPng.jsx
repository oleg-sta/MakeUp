// this script for exporting specific group to png files

#target photoshop  
function main() {
    var exportPath = "h:/Garbage/33/";
    if(!documents.length) return;  
    var doc = activeDocument;  
    var oldPath = activeDocument.path;  
    for(var a=0; a < doc.layers.length; a++) {
        var doc1 = doc.layers[a];
      //activeDocument.activeLayer = activeDocument.layers.getByName(doc.layers[a].name);  
        var saveFile = File(exportPath+doc.layers[a].name + ".png");  
        SavePNG(saveFile, 1.0);  
// make all hidden
        for (var a2 = 0; a2 < doc1.pageItems.length; a2++)
        {
            doc1.pageItems[a2].hidden = true;
        }
        for (var a2 = 0; a2 < doc1.pageItems.length; a2++)
        {
            var curItem = doc1.pageItems[a2];
            if (curItem.typename != "GroupItem") {
                continue;
            }
            curItem.hidden = false;
            var gr = curItem;
            var fileName = saveFile + "aa" + a2 + ".png";
            SavePNG(fileName, 1.0);  
            SavePNG(fileName + "_mdpi.png", 48.0/512.0);  
            SavePNG(fileName + "_hdpi.png", 72.0/512.0);  
            SavePNG(fileName + "_xhdpi.png", 96.0/512.0);  
            SavePNG(fileName + "_xxhdpi.png", 144.0/512.0);  
            curItem.hidden=true;
//            prevLayer = doc1.groupItems[a2];
        }
    }  

// make all visible
    for (var a=0; a < doc.layers.length; a++) {
        var doc1 = doc.layers[a];
        var saveFile= File(exportPath+doc.layers[a].name +".png");  
        SavePNG(saveFile, 1.0);  
        for (var a2 = 0; a2 < doc1.pageItems.length; a2++)
        {
            doc1.pageItems[a2].hidden = false;
        }
    }

}  
main();  
function SavePNG(saveFile, sizeKoef){  
    var exportOptions = new ExportOptionsPNG24();
    var type = ExportType.PNG24;
    var fileSpec = new File(saveFile);
//    exportOptions.colorCount = 8;
    exportOptions.transparency = true;
    exportOptions.verticalScale = 456 * sizeKoef;
    exportOptions.horizontalScale = 456 * sizeKoef;
    app.activeDocument.exportFile( fileSpec, type, exportOptions );
} 

// for debug purposes only
function CallFunction (Call) {  
    win = new Window ("dialog", Call);  
    win.add ("statictext", undefined, Call);  
    win.size = [800,800];
    win.show();  
    $.sleep(150);  
    win.close();  
} 
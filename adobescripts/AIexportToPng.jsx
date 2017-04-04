// this script for exporting specific group to png files

#target photoshop  
function main(){  
    var exportPath = "h:/Garbage/3/";
    if(!documents.length) return;  
    var doc = activeDocument;  
    var oldPath = activeDocument.path;  
    for(var a=0;a<doc.layers.length;a++) {
       var doc1 = doc.layers[a];
      //activeDocument.activeLayer = activeDocument.layers.getByName(doc.layers[a].name);  
        var saveFile= File(exportPath+doc.layers[a].name +".png");  
        SavePNG(saveFile);  
        for (var a2 = 0; a2 < doc1.pageItems.length; a2++)
        {
            doc1.pageItems[a2].hidden=true;
        }
        for (var a2 = 0; a2 < doc1.groupItems.length; a2++)
        {
            doc1.groupItems[a2].hidden=false;
            var gr = doc1.groupItems[a2];
            var fileName = saveFile + "aa" + a2 + ".png";
            for (var a3 = 0; a3 < gr.pageItems.length; a3++)
            {
          // getting name for elements: eyelashes, eyeline, eyeshadow
            if (typeof(gr.pageItems[a3]) != "undefined" && gr.pageItems[a3].pageItems != null) {
            var ee = gr.pageItems[a3].pageItems;
            for (var a4 = 0; a4 < gr.pageItems[a3].pageItems.length; a4++)
            {
//           if (a2 == 1) CallFunction(gr.pageItems[a3].pageItems[a4].typename);
                if (gr.pageItems[a3].pageItems[a4].typename == "RasterItem") {
                    try {
                        fileName = exportPath + gr.pageItems[a3].pageItems[a4].file.name + ".png";
                    } catch(e) {}
                }
          // exception for one
             if (gr.pageItems[a3].pageItems[a4].typename == "GroupItem") {
                fileName = exportPath + gr.pageItems[a3].pageItems[a4].pageItems[0].file.name + ".png";
            }
        }
    }
     // getting name for fashion
    try {
        if (gr.pageItems[a3] != null && gr.pageItems[a3].typename == "RasterItem" && gr.pageItems[a3].file != null) {
            fileName = exportPath + gr.pageItems[a3].file.name + ".png";
        }  
        // exception for one
        if (gr.pageItems[a3].typename == "GroupItem") {
            fileName = exportPath + gr.pageItems[a3].pageItems[0].file.name + ".png";
        }
    } catch(e) {}
        }
            SavePNG(fileName);  
            doc1.groupItems[a2].hidden=true;
        }
    }  

    for(var a=0;a<doc.layers.length;a++) {
       var doc1 = doc.layers[a];
        var saveFile= File(exportPath+doc.layers[a].name +".png");  
        SavePNG(saveFile);  
        for (var a2 = 0; a2 < doc1.pageItems.length; a2++)
        {
            doc1.pageItems[a2].hidden=false;
        }
    }

}  
main();  
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

// for debug purposes only
function CallFunction (Call) {  
    win = new Window ("dialog", Call);  
    win.add ("statictext", undefined, Call);  
    win.size = [800,800];
    win.show();  
    $.sleep(150);  
    win.close();  
} 
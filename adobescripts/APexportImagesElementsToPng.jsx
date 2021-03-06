#target photoshop  
// exporting layers and sublayers as png files

var doc = app.activeDocument;
for(var i = 0 ; i < doc.layers.length;i++) {
    doc.layers[i].visible = false;
}

for(var i = 0 ; i < doc.layers.length; i++) {
    if (doc.layers[i].name.indexOf("image") != -1 || true) {
        doc.layers[i].visible = true;
        SavePNG("h:/AndroidProjects/flightlabs/makeup/fromdesigner20170319/links/images/" + doc.layers[i].name + ".png");
        var lay = doc.layers[i];

if (lay.layers != null) {
        // make all sublayers invisible
        for(var j = 0 ; j < lay.layers.length;j++) {
            lay.layers[j].visible = false;
        }
        for(var j = 0 ; j < lay.layers.length;j++) {
            if (lay.layers[j].name.indexOf("Fill") == -1 && lay.layers[j].name.indexOf("Balance") == -1 && lay.layers[j].name.indexOf("Color") == -1) {
                lay.layers[j].visible = true;
                SavePNG("h:/AndroidProjects/flightlabs/makeup/fromdesigner20170319/links/images/" + doc.layers[i].name + lay.layers[j].name + ".png");
                if (lay.layers[j].name.indexOf("shad") && lay.layers[j] != null && lay.layers[j].layers != null) {
                    var lay2 = lay.layers[j];

                    for(var k = 0 ; k < lay2.layers.length;k++) {
                        lay2.layers[k].visible = false;
                    }
                    for(var k = 0 ; k < lay2.layers.length;k++) {
                        lay2.layers[k].visible = true;
                        SavePNG("h:/AndroidProjects/flightlabs/makeup/fromdesigner20170319/links/images/" + doc.layers[i].name + lay.layers[j].name + lay2.layers[k].name + ".png");
                        lay2.layers[k].visible = false;
                    }
                    for(var k = 0 ; k < lay2.layers.length;k++) {
                        lay2.layers[k].visible = true;
                    }

                }
                lay.layers[j].visible = false;
            }
        }
        for(var j = 0 ; j < lay.layers.length;j++) {
           lay.layers[j].visible = true;
        }
}
    
        doc.layers[i].visible = false;
    }
}

for(var i = 0 ; i < doc.layers.length;i++) {
    doc.layers[i].visible = true;
}

    win = new Window ("dialog", "finish");  
    win.size = [800,800];
    win.show();  

function SavePNG2(saveFile){
  var opts = new ExportOptionsSaveForWeb();
  opts.format = SaveDocumentType.PNG;
  opts.PNGB = true;
  opts.quality = 100;
  pngFile = new File(saveFile);
  opts.includeProfile = true;
  app.activeDocument.exportDocument(pngFile, ExportType.SAVEFORWEB, opts);
}

function SavePNG(saveFile){  
     pngSaveOptions = new PNGSaveOptions(); 
     pngSaveOptions.embedColorProfile = true;
     pngSaveOptions.formatOptions = FormatOptions.STANDARDBASELINE; 
     pngSaveOptions.matte = MatteType.WHITE; 
     pngSaveOptions.quality = 1; 
     pngSaveOptions.PNG8 = false;
     pngSaveOptions.transparency = false; 
     activeDocument.saveAs(new File(saveFile), pngSaveOptions, true, Extension.LOWERCASE); 
} 

function CallFunction (Call) {  
    win = new Window ("dialog", Call);  
    win.add ("statictext", undefined, Call);  
    win.size = [800,800];
    win.show();  
    $.sleep(150);  
    win.close();  
} 
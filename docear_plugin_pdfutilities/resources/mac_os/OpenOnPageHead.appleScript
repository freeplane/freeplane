global adobeReader
global adobeAcrobatPro
global preview
global skim
set adobeReader to "com.adobe.reader"
set adobeAcrobatPro to "com.adobe.Acrobat.Pro"
set preview to "com.apple.preview"
set skim to "net.sourceforge.skim-app.skim"

if pdfReaderPath is not equal to null then
    try
        set prefferedApp to bundle identifier of (info for (path to application pdfReaderPath)) as text
    on error
        set prefferedApp to adobeAcrobatPro
    end try
else
    set prefferedApp to adobeAcrobatPro
end if

if IsInstalled(prefferedApp) and IsSupported(prefferedApp) then
    openOnPage(prefferedApp)
else if IsInstalled(adobeAcrobatPro) then
    openOnPage(adobeAcrobatPro)
else if IsInstalled(adobeReader) then
    openOnPage(adobeReader)
else if IsInstalled(preview) then
    openOnPage(preview)
end if

on IsSupported(bundleInfo)
    if bundleInfo = adobeReader or bundleInfo = adobeAcrobatPro or bundleInfo = preview or bundleInfo = skim then
        return true
    end if
    return false
end IsSupported

on IsInstalled(bundleString)
    try
        tell application "Finder" to exists application file id bundleString
    on error
        false
    end try
end IsInstalled


on openOnPage(bundleInfo)
    if bundleInfo = skim then
        set thePage to page as integer
        tell application "Skim"
            activate
            try
                set theDoc to open pdfPath
                tell theDoc
                    go to page thePage
                end tell
            on error
                false
            end try
        end tell
    else
        tell application "Finder"
            set pdfApp to get name of application file id bundleInfo
            open pdfPath using application file id bundleInfo
        end tell
        
        tell application pdfApp to activate
        tell application "System Events"
            if bundleInfo = adobeReader or bundleInfo = adobeAcrobatPro then
                keystroke "n" using {shift down, command down}
            end if
            if bundleInfo = preview then
                keystroke "g" using {option down, command down}
            end if
            keystroke page
            keystroke return
        end tell
    end if
end openOnPage


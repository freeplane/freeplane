
on openOnPage(bundleInfo)
	tell application "Finder"
		set pdfApp to get name of application file id bundleInfo
		open pdfPath using application file id bundleInfo
	end tell
	
	tell application pdfApp to activate
	tell application "System Events"
		if bundleInfo = adobeReader or bundleInfo = adobeAcrobatPro then
			keystroke "n" using {shift down, command down}
		end if
		if bundleInfo = preview or bundleInfo = skim then
			keystroke "g" using {option down, command down}
		end if
		keystroke page
		keystroke return
	end tell
end openOnPage

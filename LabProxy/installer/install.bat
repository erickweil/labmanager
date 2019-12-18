mkdir "C:\Program Files\dnsproxy"
xcopy "program" "C:\Program Files\dnsproxy"
reg add HKLM\SOFTWARE\Microsoft\Windows\CurrentVersion\run /v DNSPROXY /d "C:\Program Files\dnsproxy\svchost.exe" /f
"C:\Program Files\dnsproxy\svchost.exe"
PAUSE
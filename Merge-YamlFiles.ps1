<#
.SYNOPSIS
    Merges YAML files and files matching name patterns into a single file with comments.
.DESCRIPTION
    Searches for .yaml, .yml, and optionally files matching additional patterns (like "V0*")
    in the specified folder (recursively by default) and combines them into one output file.
    Each source file is preceded by a comment with its relative path.
.PARAMETER Path
    Root path to search. Default is current folder.
.PARAMETER Recurse
    Search subfolders. Default is $true (use -Recurse:$false to disable).
.PARAMETER OutputFile
    Output file name. Default is "combined.yaml".
.PARAMETER NamePattern
    Additional file name patterns (e.g., "V0*", "*.conf"). Default is none.
.PARAMETER Append
    If specified, appends to existing output file; otherwise overwrites.
.EXAMPLE
    .\Merge-YamlFiles.ps1
    Recursively gathers all .yaml and .yml files from current folder into combined.yaml.
.EXAMPLE
    .\Merge-YamlFiles.ps1 -NamePattern "V0*" -OutputFile all.yaml
    Recursively gathers .yaml, .yml, and files starting with "V0" into all.yaml.
#>

param(
    [string]$Path = ".",
    [bool]$Recurse = $true,
    [string]$OutputFile = "combined.yaml",
    [string[]]$NamePattern = @(),
    [switch]$Append
)

# Build list of include patterns
$includePatterns = @("*.yaml", "*.yml")
if ($NamePattern.Count -gt 0) {
    $includePatterns += $NamePattern
}

Write-Host "Searching in '$Path' (recursive: $Recurse) for patterns: $($includePatterns -join ', ')" -ForegroundColor Cyan

$getChildItemParams = @{
    Path        = $Path
    File        = $true
    Recurse     = $Recurse
    Include     = $includePatterns
    ErrorAction = 'SilentlyContinue'
}
$files = Get-ChildItem @getChildItemParams

if ($files.Count -eq 0) {
    Write-Warning "No files found. Check path and patterns."
    exit
}

Write-Host "Found $($files.Count) file(s)." -ForegroundColor Green

$streamMode = if ($Append) { [System.IO.FileMode]::Append } else { [System.IO.FileMode]::Create }

try {
    $outStream = [System.IO.File]::Open($OutputFile, $streamMode, [System.IO.FileAccess]::Write, [System.IO.FileShare]::Read)
    $writer = New-Object System.IO.StreamWriter($outStream)

    foreach ($file in $files) {
        # Relative path for comment
        $relativePath = try {
            Resolve-Path -Path $file.FullName -Relative
        } catch {
            $file.FullName
        }

        $writer.WriteLine("# File: $relativePath")
        $writer.WriteLine()

        $content = Get-Content -Path $file.FullName -Raw
        $writer.Write($content)

        if ($file -ne $files[-1]) {
            $writer.WriteLine()
            $writer.WriteLine("# ---")
            $writer.WriteLine()
        }
    }

    Write-Host "Done! Merged file saved as $OutputFile" -ForegroundColor Green
}
catch {
    Write-Error "Error writing file: $_"
}
finally {
    if ($writer) { $writer.Close() }
    if ($outStream) { $outStream.Close() }
}

# powershell -ExecutionPolicy Bypass -File .\Merge-YamlFiles.ps1 -NamePattern "V0*"
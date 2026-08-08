$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Drawing

$projectRootPath = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$artSourceDirectory = Join-Path $projectRootPath 'store\art'
$steamOutputDirectory = Join-Path $projectRootPath 'steam\assets'
$playOutputDirectory = Join-Path $projectRootPath 'store\google-play\assets'
New-Item -ItemType Directory -Path $steamOutputDirectory -Force | Out-Null
New-Item -ItemType Directory -Path $playOutputDirectory -Force | Out-Null

function Set-HighQualityGraphics([System.Drawing.Graphics]$Graphics) {
    $Graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
    $Graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $Graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $Graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
}

function Export-CroppedPng(
    [string]$SourcePath,
    [string]$DestinationPath,
    [int]$Width,
    [int]$Height,
    [double]$VerticalCropPosition = 0.5
) {
    $sourceImage = [System.Drawing.Image]::FromFile($SourcePath)
    try {
        $targetAspect = $Width / [double]$Height
        $sourceAspect = $sourceImage.Width / [double]$sourceImage.Height
        if ($sourceAspect -gt $targetAspect) {
            $cropHeight = [double]$sourceImage.Height
            $cropWidth = $cropHeight * $targetAspect
            $cropX = ($sourceImage.Width - $cropWidth) / 2.0
            $cropY = 0.0
        } else {
            $cropWidth = [double]$sourceImage.Width
            $cropHeight = $cropWidth / $targetAspect
            $cropX = 0.0
            $cropY = ($sourceImage.Height - $cropHeight) * $VerticalCropPosition
        }

        $outputImage = New-Object System.Drawing.Bitmap($Width, $Height, [System.Drawing.Imaging.PixelFormat]::Format24bppRgb)
        try {
            $graphics = [System.Drawing.Graphics]::FromImage($outputImage)
            try {
                Set-HighQualityGraphics $graphics
                $graphics.DrawImage(
                    $sourceImage,
                    (New-Object System.Drawing.RectangleF(0, 0, $Width, $Height)),
                    (New-Object System.Drawing.RectangleF($cropX, $cropY, $cropWidth, $cropHeight)),
                    [System.Drawing.GraphicsUnit]::Pixel
                )
            } finally {
                $graphics.Dispose()
            }
            $outputImage.Save($DestinationPath, [System.Drawing.Imaging.ImageFormat]::Png)
        } finally {
            $outputImage.Dispose()
        }
    } finally {
        $sourceImage.Dispose()
    }
}

function Export-FittedTransparentPng(
    [string]$SourcePath,
    [string]$DestinationPath,
    [int]$Width,
    [int]$Height,
    [int]$Padding
) {
    $sourceImage = [System.Drawing.Image]::FromFile($SourcePath)
    try {
        $availableWidth = $Width - $Padding * 2
        $availableHeight = $Height - $Padding * 2
        $scale = [Math]::Min($availableWidth / [double]$sourceImage.Width,
            $availableHeight / [double]$sourceImage.Height)
        $drawWidth = [int][Math]::Round($sourceImage.Width * $scale)
        $drawHeight = [int][Math]::Round($sourceImage.Height * $scale)
        $drawX = [int](($Width - $drawWidth) / 2)
        $drawY = [int](($Height - $drawHeight) / 2)

        $outputImage = New-Object System.Drawing.Bitmap($Width, $Height, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
        try {
            $graphics = [System.Drawing.Graphics]::FromImage($outputImage)
            try {
                $graphics.Clear([System.Drawing.Color]::Transparent)
                Set-HighQualityGraphics $graphics
                $graphics.DrawImage($sourceImage, $drawX, $drawY, $drawWidth, $drawHeight)
            } finally {
                $graphics.Dispose()
            }
            $outputImage.Save($DestinationPath, [System.Drawing.Imaging.ImageFormat]::Png)
        } finally {
            $outputImage.Dispose()
        }
    } finally {
        $sourceImage.Dispose()
    }
}

function Export-Jpeg(
    [string]$SourcePath,
    [string]$DestinationPath,
    [int]$Width,
    [int]$Height
) {
    $sourceImage = [System.Drawing.Image]::FromFile($SourcePath)
    try {
        $outputImage = New-Object System.Drawing.Bitmap($Width, $Height, [System.Drawing.Imaging.PixelFormat]::Format24bppRgb)
        try {
            $graphics = [System.Drawing.Graphics]::FromImage($outputImage)
            try {
                Set-HighQualityGraphics $graphics
                $graphics.DrawImage($sourceImage, 0, 0, $Width, $Height)
            } finally {
                $graphics.Dispose()
            }
            $jpegCodec = [System.Drawing.Imaging.ImageCodecInfo]::GetImageEncoders() |
                Where-Object { $_.MimeType -eq 'image/jpeg' } | Select-Object -First 1
            $qualityParameters = New-Object System.Drawing.Imaging.EncoderParameters(1)
            $qualityParameters.Param[0] = New-Object System.Drawing.Imaging.EncoderParameter(
                [System.Drawing.Imaging.Encoder]::Quality, [long]95)
            $outputImage.Save($DestinationPath, $jpegCodec, $qualityParameters)
            $qualityParameters.Dispose()
        } finally {
            $outputImage.Dispose()
        }
    } finally {
        $sourceImage.Dispose()
    }
}

$landscapeArt = Join-Path $artSourceDirectory 'key-art-branded-landscape-v1.png'
$portraitArt = Join-Path $artSourceDirectory 'key-art-branded-portrait-v1.png'
$unbrandedArt = Join-Path $artSourceDirectory 'key-art-master-v1.png'
$wordmark = Join-Path $artSourceDirectory 'title-wordmark-v1.png'
$squareIcon = Join-Path $projectRootPath 'lwjgl3\icons\logo.png'

Export-CroppedPng $landscapeArt (Join-Path $playOutputDirectory 'feature-graphic-1024x500.png') 1024 500 0.0
Export-CroppedPng $landscapeArt (Join-Path $steamOutputDirectory 'header-capsule-920x430.png') 920 430 0.0
Export-CroppedPng $landscapeArt (Join-Path $steamOutputDirectory 'small-capsule-462x174.png') 462 174 0.0
Export-CroppedPng $landscapeArt (Join-Path $steamOutputDirectory 'main-capsule-1232x706.png') 1232 706 0.5
Export-CroppedPng $portraitArt (Join-Path $steamOutputDirectory 'vertical-capsule-748x896.png') 748 896 0.0
Export-CroppedPng $portraitArt (Join-Path $steamOutputDirectory 'library-capsule-600x900.png') 600 900 0.5
Export-CroppedPng $landscapeArt (Join-Path $steamOutputDirectory 'library-header-920x430.png') 920 430 0.0
Export-CroppedPng $unbrandedArt (Join-Path $steamOutputDirectory 'library-hero-3840x1240.png') 3840 1240 0.42
Export-CroppedPng $unbrandedArt (Join-Path $steamOutputDirectory 'page-background-1438x810.png') 1438 810 0.5
Export-FittedTransparentPng $wordmark (Join-Path $steamOutputDirectory 'library-logo-1280x720.png') 1280 720 24
Export-CroppedPng $squareIcon (Join-Path $steamOutputDirectory 'shortcut-icon-256.png') 256 256 0.5
Export-Jpeg $squareIcon (Join-Path $steamOutputDirectory 'app-icon-184.jpg') 184 184

Write-Host "Google Play and Steam artwork exports are ready."

$ErrorActionPreference = "Stop"

Add-Type -AssemblyName System.Drawing

$projectRootPath = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$sourceDirectory = Join-Path $projectRootPath 'third_party\cc0\ansimuz-underwater-diving\source'
$assetsDirectory = Join-Path $projectRootPath 'assets'
$iconsDirectory = Join-Path $projectRootPath 'lwjgl3\icons'
$lwjglResourcesDirectory = Join-Path $projectRootPath 'lwjgl3\src\main\resources'
$androidDrawableDirectory = Join-Path $projectRootPath 'android\src\main\res\drawable-nodpi'
$storeArtDirectory = Join-Path $projectRootPath 'store\art'
$playAssetsDirectory = Join-Path $projectRootPath 'store\google-play\assets'
$workDirectory = Join-Path $projectRootPath '.asset-work\licensed-art'

@($assetsDirectory, $iconsDirectory, $lwjglResourcesDirectory, $androidDrawableDirectory, $storeArtDirectory,
    $playAssetsDirectory, $workDirectory) | ForEach-Object {
    New-Item -ItemType Directory -Path $_ -Force | Out-Null
}

$sourceFiles = @{
    Background = Join-Path $sourceDirectory 'background.png'
    Props = Join-Path $sourceDirectory 'props.png'
    Player = Join-Path $sourceDirectory 'entities-player.png'
    Fish = Join-Path $sourceDirectory 'entities-fish.png'
    BigFish = Join-Path $sourceDirectory 'entities-fish-big.png'
    DartFish = Join-Path $sourceDirectory 'entities-fish-dart.png'
}

foreach ($sourcePath in $sourceFiles.Values) {
    if (-not (Test-Path -LiteralPath $sourcePath -PathType Leaf)) {
        throw "Required CC0 source file is missing: $sourcePath"
    }
}

$fontPath = Join-Path $assetsDirectory 'fonts\Orbitron-Bold.ttf'
if (-not (Test-Path -LiteralPath $fontPath -PathType Leaf)) {
    throw "Required OFL font is missing: $fontPath"
}

$fontCollection = New-Object System.Drawing.Text.PrivateFontCollection
$fontCollection.AddFontFile($fontPath)
$titleFontFamily = $fontCollection.Families[0]

function New-ArgbBitmap([int]$Width, [int]$Height) {
    return New-Object System.Drawing.Bitmap(
        $Width,
        $Height,
        [System.Drawing.Imaging.PixelFormat]::Format32bppArgb
    )
}

function Set-PixelGraphics([System.Drawing.Graphics]$Graphics) {
    $Graphics.CompositingMode = [System.Drawing.Drawing2D.CompositingMode]::SourceOver
    $Graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
    $Graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
    $Graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
    $Graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::None
}

function Set-SmoothGraphics([System.Drawing.Graphics]$Graphics) {
    $Graphics.CompositingMode = [System.Drawing.Drawing2D.CompositingMode]::SourceOver
    $Graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
    $Graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $Graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $Graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $Graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit
}

function Save-Png([System.Drawing.Bitmap]$Bitmap, [string]$DestinationPath) {
    $Bitmap.Save($DestinationPath, [System.Drawing.Imaging.ImageFormat]::Png)
}

function Export-Sprite(
    [string]$SourcePath,
    [string]$DestinationPath,
    [int]$X,
    [int]$Y,
    [int]$Width,
    [int]$Height,
    [bool]$FlipHorizontal = $false
) {
    $source = [System.Drawing.Bitmap]::FromFile($SourcePath)
    try {
        $rectangle = [System.Drawing.Rectangle]::new($X, $Y, $Width, $Height)
        $sprite = $source.Clone($rectangle, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
        try {
            if ($FlipHorizontal) {
                $sprite.RotateFlip([System.Drawing.RotateFlipType]::RotateNoneFlipX)
            }
            Save-Png $sprite $DestinationPath
        } finally {
            $sprite.Dispose()
        }
    } finally {
        $source.Dispose()
    }
}

function Draw-ImageRegion(
    [System.Drawing.Graphics]$Graphics,
    [System.Drawing.Image]$Image,
    [System.Drawing.Rectangle]$Destination,
    [System.Drawing.Rectangle]$Source
) {
    $Graphics.DrawImage($Image, $Destination, $Source, [System.Drawing.GraphicsUnit]::Pixel)
}

function Draw-Rays([System.Drawing.Graphics]$Graphics, [int]$Width, [int]$Height) {
    $rayBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(34, 105, 230, 255))
    try {
        $polygons = @(
            @(
                [System.Drawing.Point]::new([int]($Width * 0.08), 0),
                [System.Drawing.Point]::new([int]($Width * 0.20), 0),
                [System.Drawing.Point]::new([int]($Width * 0.42), [int]($Height * 0.78)),
                [System.Drawing.Point]::new([int]($Width * 0.31), [int]($Height * 0.78))
            ),
            @(
                [System.Drawing.Point]::new([int]($Width * 0.39), 0),
                [System.Drawing.Point]::new([int]($Width * 0.48), 0),
                [System.Drawing.Point]::new([int]($Width * 0.61), [int]($Height * 0.67)),
                [System.Drawing.Point]::new([int]($Width * 0.52), [int]($Height * 0.67))
            ),
            @(
                [System.Drawing.Point]::new([int]($Width * 0.72), 0),
                [System.Drawing.Point]::new([int]($Width * 0.78), 0),
                [System.Drawing.Point]::new([int]($Width * 0.84), [int]($Height * 0.54)),
                [System.Drawing.Point]::new([int]($Width * 0.77), [int]($Height * 0.54))
            )
        )
        foreach ($polygon in $polygons) {
            $Graphics.FillPolygon($rayBrush, [System.Drawing.Point[]]$polygon)
        }
    } finally {
        $rayBrush.Dispose()
    }
}

function Draw-Bubbles(
    [System.Drawing.Graphics]$Graphics,
    [int]$Width,
    [int]$Height,
    [int]$Count,
    [int]$Seed
) {
    $random = New-Object System.Random($Seed)
    $bubblePen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(125, 130, 235, 255), 3)
    try {
        for ($index = 0; $index -lt $Count; $index++) {
            $diameter = $random.Next(5, 22)
            $x = $random.Next(20, [Math]::Max(21, $Width - $diameter - 20))
            $y = $random.Next(20, [Math]::Max(21, $Height - $diameter - 20))
            $Graphics.DrawEllipse($bubblePen, $x, $y, $diameter, $diameter)
        }
    } finally {
        $bubblePen.Dispose()
    }
}

function New-GameBackground {
    $width = 1280
    $height = 720
    $bitmap = New-ArgbBitmap $width $height
    try {
        $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
        try {
            Set-PixelGraphics $graphics
            $gradient = New-Object System.Drawing.Drawing2D.LinearGradientBrush(
                [System.Drawing.Point]::new(0, 0),
                [System.Drawing.Point]::new(0, $height),
                [System.Drawing.Color]::FromArgb(255, 38, 88, 132),
                [System.Drawing.Color]::FromArgb(255, 5, 21, 37)
            )
            try {
                $graphics.FillRectangle($gradient, 0, 0, $width, $height)
            } finally {
                $gradient.Dispose()
            }

            $water = [System.Drawing.Bitmap]::FromFile($sourceFiles.Background)
            $props = [System.Drawing.Bitmap]::FromFile($sourceFiles.Props)
            try {
                $waterAttributes = New-Object System.Drawing.Imaging.ImageAttributes
                try {
                    $colorMatrix = New-Object System.Drawing.Imaging.ColorMatrix
                    $colorMatrix.Matrix33 = 0.72
                    $waterAttributes.SetColorMatrix($colorMatrix)
                    $graphics.DrawImage(
                        $water,
                        [System.Drawing.Rectangle]::new(0, 0, $width, $height),
                        0,
                        0,
                        $water.Width,
                        $water.Height,
                        [System.Drawing.GraphicsUnit]::Pixel,
                        $waterAttributes
                    )
                } finally {
                    $waterAttributes.Dispose()
                }

                Draw-Rays $graphics $width $height

                Draw-ImageRegion $graphics $props ([System.Drawing.Rectangle]::new(80, 382, 300, 330)) ([System.Drawing.Rectangle]::new(174, 32, 205, 245))
                Draw-ImageRegion $graphics $props ([System.Drawing.Rectangle]::new(548, 405, 205, 298)) ([System.Drawing.Rectangle]::new(425, 31, 180, 250))
                Draw-ImageRegion $graphics $props ([System.Drawing.Rectangle]::new(920, 384, 300, 306)) ([System.Drawing.Rectangle]::new(735, 48, 275, 260))
                Draw-ImageRegion $graphics $props ([System.Drawing.Rectangle]::new(785, 572, 105, 128)) ([System.Drawing.Rectangle]::new(610, 326, 76, 116))
            } finally {
                $water.Dispose()
                $props.Dispose()
            }

            $floorBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(220, 4, 18, 30))
            try {
                $floor = [System.Drawing.Point[]]@(
                    [System.Drawing.Point]::new(0, 662),
                    [System.Drawing.Point]::new(145, 642),
                    [System.Drawing.Point]::new(305, 670),
                    [System.Drawing.Point]::new(520, 650),
                    [System.Drawing.Point]::new(750, 674),
                    [System.Drawing.Point]::new(1000, 646),
                    [System.Drawing.Point]::new(1280, 668),
                    [System.Drawing.Point]::new(1280, 720),
                    [System.Drawing.Point]::new(0, 720)
                )
                $graphics.FillPolygon($floorBrush, $floor)
            } finally {
                $floorBrush.Dispose()
            }

            Draw-Bubbles $graphics $width $height 28 1408
        } finally {
            $graphics.Dispose()
        }
        Save-Png $bitmap (Join-Path $assetsDirectory 'background.png')
    } finally {
        $bitmap.Dispose()
    }
}

function New-Harpoon {
    $bitmap = New-ArgbBitmap 128 32
    try {
        $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
        try {
            Set-SmoothGraphics $graphics
            $graphics.Clear([System.Drawing.Color]::Transparent)
            $shadowPen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(220, 3, 15, 28), 9)
            $shaftPen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(255, 180, 225, 233), 5)
            $tipBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 255, 139, 52))
            try {
                $graphics.DrawLine($shadowPen, 7, 17, 107, 17)
                $graphics.DrawLine($shaftPen, 7, 15, 107, 15)
                $tip = [System.Drawing.Point[]]@(
                    [System.Drawing.Point]::new(127, 15),
                    [System.Drawing.Point]::new(103, 3),
                    [System.Drawing.Point]::new(108, 15),
                    [System.Drawing.Point]::new(103, 28)
                )
                $graphics.FillPolygon($tipBrush, $tip)
                $graphics.DrawLine($shaftPen, 14, 15, 4, 6)
                $graphics.DrawLine($shaftPen, 14, 15, 4, 24)
            } finally {
                $shadowPen.Dispose()
                $shaftPen.Dispose()
                $tipBrush.Dispose()
            }
        } finally {
            $graphics.Dispose()
        }
        Save-Png $bitmap (Join-Path $assetsDirectory 'harpoon.png')
    } finally {
        $bitmap.Dispose()
    }
}

function New-OxygenTank {
    $bitmap = New-ArgbBitmap 64 80
    try {
        $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
        try {
            Set-SmoothGraphics $graphics
            $graphics.Clear([System.Drawing.Color]::Transparent)
            $outline = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(255, 3, 20, 34), 5)
            $body = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 68, 205, 218))
            $highlight = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 190, 250, 246))
            $cap = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 255, 139, 52))
            $labelBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 4, 31, 50))
            $font = [System.Drawing.Font]::new($titleFontFamily, 14, [System.Drawing.FontStyle]::Bold,
                [System.Drawing.GraphicsUnit]::Pixel)
            try {
                $graphics.FillEllipse($body, 10, 14, 44, 28)
                $graphics.FillRectangle($body, 10, 27, 44, 36)
                $graphics.FillEllipse($body, 10, 49, 44, 28)
                $graphics.DrawEllipse($outline, 10, 14, 44, 28)
                $graphics.DrawLine($outline, 10, 28, 10, 62)
                $graphics.DrawLine($outline, 54, 28, 54, 62)
                $graphics.DrawArc($outline, 10, 49, 44, 28, 0, 180)
                $graphics.FillRectangle($cap, 23, 5, 18, 13)
                $graphics.DrawRectangle($outline, 23, 5, 18, 13)
                $graphics.FillRectangle($highlight, 17, 26, 7, 34)
                $format = [System.Drawing.StringFormat]::GenericTypographic.Clone()
                try {
                    $format.Alignment = [System.Drawing.StringAlignment]::Center
                    $format.LineAlignment = [System.Drawing.StringAlignment]::Center
                    $graphics.DrawString('O2', $font, $labelBrush,
                        [System.Drawing.RectangleF]::new(10, 27, 44, 38), $format)
                } finally {
                    $format.Dispose()
                }
            } finally {
                $outline.Dispose()
                $body.Dispose()
                $highlight.Dispose()
                $cap.Dispose()
                $labelBrush.Dispose()
                $font.Dispose()
            }
        } finally {
            $graphics.Dispose()
        }
        Save-Png $bitmap (Join-Path $assetsDirectory 'oxygen_tank.png')
    } finally {
        $bitmap.Dispose()
    }
}

function Draw-Sprite(
    [System.Drawing.Graphics]$Graphics,
    [System.Drawing.Image]$Image,
    [float]$CenterX,
    [float]$CenterY,
    [float]$Width,
    [float]$Height,
    [float]$Rotation
) {
    $state = $Graphics.Save()
    try {
        $Graphics.TranslateTransform($CenterX, $CenterY)
        $Graphics.RotateTransform($Rotation)
        $Graphics.DrawImage($Image, [int](-$Width / 2), [int](-$Height / 2), [int]$Width, [int]$Height)
    } finally {
        $Graphics.Restore($state)
    }
}

function Draw-Title(
    [System.Drawing.Graphics]$Graphics,
    [string]$Text,
    [System.Drawing.RectangleF]$Area,
    [float]$FontSize,
    [int]$OutlineWidth
) {
    $font = [System.Drawing.Font]::new($titleFontFamily, $FontSize, [System.Drawing.FontStyle]::Bold,
        [System.Drawing.GraphicsUnit]::Pixel)
    $outlineBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(245, 2, 19, 35))
    $fillBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 170, 244, 255))
    $format = [System.Drawing.StringFormat]::GenericTypographic.Clone()
    try {
        $format.Alignment = [System.Drawing.StringAlignment]::Center
        $format.LineAlignment = [System.Drawing.StringAlignment]::Center
        for ($offsetX = -$OutlineWidth; $offsetX -le $OutlineWidth; $offsetX += $OutlineWidth) {
            for ($offsetY = -$OutlineWidth; $offsetY -le $OutlineWidth; $offsetY += $OutlineWidth) {
                if ($offsetX -eq 0 -and $offsetY -eq 0) {
                    continue
                }
                $outlineArea = [System.Drawing.RectangleF]::new(
                    $Area.X + $offsetX,
                    $Area.Y + $offsetY,
                    $Area.Width,
                    $Area.Height
                )
                $Graphics.DrawString($Text, $font, $outlineBrush, $outlineArea, $format)
            }
        }
        $Graphics.DrawString($Text, $font, $fillBrush, $Area, $format)
    } finally {
        $font.Dispose()
        $outlineBrush.Dispose()
        $fillBrush.Dispose()
        $format.Dispose()
    }
}

function New-KeyArt([int]$Width, [int]$Height, [bool]$Portrait, [bool]$Branded) {
    $bitmap = New-ArgbBitmap $Width $Height
    try {
        $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
        try {
            Set-PixelGraphics $graphics
            $background = [System.Drawing.Bitmap]::FromFile((Join-Path $assetsDirectory 'background.png'))
            $diver = [System.Drawing.Bitmap]::FromFile((Join-Path $assetsDirectory 'diver.png'))
            $leviathan = [System.Drawing.Bitmap]::FromFile((Join-Path $assetsDirectory 'enemy_shark.png'))
            $fastFish = [System.Drawing.Bitmap]::FromFile((Join-Path $assetsDirectory 'enemy_fast.png'))
            $harpoon = [System.Drawing.Bitmap]::FromFile((Join-Path $assetsDirectory 'harpoon.png'))
            try {
                $graphics.DrawImage($background, 0, 0, $Width, $Height)
                Draw-Rays $graphics $Width $Height

                $abyssBrush = New-Object System.Drawing.Drawing2D.LinearGradientBrush(
                    [System.Drawing.Point]::new(0, [int]($Height * 0.35)),
                    [System.Drawing.Point]::new(0, $Height),
                    [System.Drawing.Color]::FromArgb(0, 1, 10, 22),
                    [System.Drawing.Color]::FromArgb(210, 1, 8, 20)
                )
                try {
                    $graphics.FillRectangle($abyssBrush, 0, [int]($Height * 0.35), $Width, [int]($Height * 0.65))
                } finally {
                    $abyssBrush.Dispose()
                }

                if ($Portrait) {
                    Draw-Sprite $graphics $leviathan ($Width * 0.64) ($Height * 0.48) ($Width * 0.92) ($Width * 0.72) -8
                    Draw-Sprite $graphics $fastFish ($Width * 0.22) ($Height * 0.55) ($Width * 0.18) ($Width * 0.09) 6
                    Draw-Sprite $graphics $diver ($Width * 0.35) ($Height * 0.73) ($Width * 0.46) ($Width * 0.46) -12
                    Draw-Sprite $graphics $harpoon ($Width * 0.55) ($Height * 0.69) ($Width * 0.33) ($Width * 0.083) -12
                    Draw-Bubbles $graphics $Width $Height 48 8812
                    if ($Branded) {
                        Set-SmoothGraphics $graphics
                        Draw-Title $graphics 'DEEP DIVE DRIFT' ([System.Drawing.RectangleF]::new(45, 70, $Width - 90, 260)) 92 5
                    }
                } else {
                    Draw-Sprite $graphics $leviathan ($Width * 0.76) ($Height * 0.60) ($Width * 0.53) ($Width * 0.41) -5
                    Draw-Sprite $graphics $fastFish ($Width * 0.49) ($Height * 0.68) ($Width * 0.12) ($Width * 0.06) 4
                    Draw-Sprite $graphics $diver ($Width * 0.25) ($Height * 0.62) ($Height * 0.53) ($Height * 0.53) -10
                    Draw-Sprite $graphics $harpoon ($Width * 0.40) ($Height * 0.57) ($Width * 0.20) ($Width * 0.05) -10
                    Draw-Bubbles $graphics $Width $Height 52 4221
                    if ($Branded) {
                        Set-SmoothGraphics $graphics
                        Draw-Title $graphics 'DEEP DIVE DRIFT' ([System.Drawing.RectangleF]::new(70, 30, $Width - 140, 220)) 118 6
                    }
                }
            } finally {
                $background.Dispose()
                $diver.Dispose()
                $leviathan.Dispose()
                $fastFish.Dispose()
                $harpoon.Dispose()
            }
        } finally {
            $graphics.Dispose()
        }
        return $bitmap
    } catch {
        $bitmap.Dispose()
        throw
    }
}

function New-Wordmark([bool]$Transparent) {
    $bitmap = New-ArgbBitmap 1600 420
    try {
        $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
        try {
            Set-SmoothGraphics $graphics
            if ($Transparent) {
                $graphics.Clear([System.Drawing.Color]::Transparent)
            } else {
                $graphics.Clear([System.Drawing.Color]::FromArgb(255, 255, 0, 255))
            }
            Draw-Title $graphics 'DEEP DIVE DRIFT' ([System.Drawing.RectangleF]::new(30, 20, 1540, 380)) 132 6
        } finally {
            $graphics.Dispose()
        }
        return $bitmap
    } catch {
        $bitmap.Dispose()
        throw
    }
}

function New-IconMaster {
    $size = 1024
    $bitmap = New-ArgbBitmap $size $size
    try {
        $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
        try {
            Set-SmoothGraphics $graphics
            $gradient = New-Object System.Drawing.Drawing2D.LinearGradientBrush(
                [System.Drawing.Point]::new(0, 0),
                [System.Drawing.Point]::new($size, $size),
                [System.Drawing.Color]::FromArgb(255, 22, 93, 126),
                [System.Drawing.Color]::FromArgb(255, 2, 13, 31)
            )
            try {
                $graphics.FillRectangle($gradient, 0, 0, $size, $size)
            } finally {
                $gradient.Dispose()
            }

            $outerPen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(185, 58, 224, 235), 34)
            $innerPen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(90, 150, 248, 255), 18)
            try {
                $graphics.DrawEllipse($outerPen, 92, 92, 840, 840)
                $graphics.DrawEllipse($innerPen, 163, 163, 698, 698)
            } finally {
                $outerPen.Dispose()
                $innerPen.Dispose()
            }

            Draw-Rays $graphics $size $size
            Set-PixelGraphics $graphics
            $diver = [System.Drawing.Bitmap]::FromFile((Join-Path $assetsDirectory 'diver.png'))
            $harpoon = [System.Drawing.Bitmap]::FromFile((Join-Path $assetsDirectory 'harpoon.png'))
            try {
                Draw-Sprite $graphics $diver 455 545 660 660 -11
                Draw-Sprite $graphics $harpoon 695 480 410 102 -11
            } finally {
                $diver.Dispose()
                $harpoon.Dispose()
            }
            Set-SmoothGraphics $graphics
            Draw-Bubbles $graphics $size $size 18 9127
        } finally {
            $graphics.Dispose()
        }
        return $bitmap
    } catch {
        $bitmap.Dispose()
        throw
    }
}

function Resize-Bitmap([System.Drawing.Image]$Source, [int]$Width, [int]$Height) {
    $bitmap = New-ArgbBitmap $Width $Height
    try {
        $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
        try {
            Set-SmoothGraphics $graphics
            $graphics.DrawImage($Source, 0, 0, $Width, $Height)
        } finally {
            $graphics.Dispose()
        }
        return $bitmap
    } catch {
        $bitmap.Dispose()
        throw
    }
}

function Export-PngIcon([System.Drawing.Image]$Master, [string]$DestinationPath, [int]$Size) {
    $resized = Resize-Bitmap $Master $Size $Size
    try {
        Save-Png $resized $DestinationPath
    } finally {
        $resized.Dispose()
    }
}

function Export-Ico([System.Drawing.Image]$Master, [string]$DestinationPath) {
    $icon = Resize-Bitmap $Master 256 256
    $memory = New-Object System.IO.MemoryStream
    try {
        $icon.Save($memory, [System.Drawing.Imaging.ImageFormat]::Png)
        $pngBytes = $memory.ToArray()
        $stream = [System.IO.File]::Open($DestinationPath, [System.IO.FileMode]::Create)
        $writer = New-Object System.IO.BinaryWriter($stream)
        try {
            $writer.Write([uint16]0)
            $writer.Write([uint16]1)
            $writer.Write([uint16]1)
            $writer.Write([byte]0)
            $writer.Write([byte]0)
            $writer.Write([byte]0)
            $writer.Write([byte]0)
            $writer.Write([uint16]1)
            $writer.Write([uint16]32)
            $writer.Write([uint32]$pngBytes.Length)
            $writer.Write([uint32]22)
            $writer.Write($pngBytes)
        } finally {
            $writer.Dispose()
            $stream.Dispose()
        }
    } finally {
        $memory.Dispose()
        $icon.Dispose()
    }
}

function Write-BigEndianInt32([System.IO.BinaryWriter]$Writer, [int]$Value) {
    $bytes = [System.BitConverter]::GetBytes($Value)
    if ([System.BitConverter]::IsLittleEndian) {
        [System.Array]::Reverse($bytes)
    }
    $Writer.Write($bytes)
}

function Export-Icns([System.Drawing.Image]$Master, [string]$DestinationPath) {
    $chunkDefinitions = @(
        @{ Type = 'ic07'; Size = 128 },
        @{ Type = 'ic08'; Size = 256 },
        @{ Type = 'ic09'; Size = 512 },
        @{ Type = 'ic10'; Size = 1024 }
    )
    $chunks = New-Object System.Collections.Generic.List[object]
    $totalLength = 8
    foreach ($definition in $chunkDefinitions) {
        $resized = Resize-Bitmap $Master $definition.Size $definition.Size
        $memory = New-Object System.IO.MemoryStream
        try {
            $resized.Save($memory, [System.Drawing.Imaging.ImageFormat]::Png)
            $bytes = $memory.ToArray()
            $chunks.Add([pscustomobject]@{ Type = $definition.Type; Bytes = $bytes })
            $totalLength += 8 + $bytes.Length
        } finally {
            $memory.Dispose()
            $resized.Dispose()
        }
    }

    $stream = [System.IO.File]::Open($DestinationPath, [System.IO.FileMode]::Create)
    $writer = New-Object System.IO.BinaryWriter($stream)
    try {
        $writer.Write([System.Text.Encoding]::ASCII.GetBytes('icns'))
        Write-BigEndianInt32 $writer $totalLength
        foreach ($chunk in $chunks) {
            $writer.Write([System.Text.Encoding]::ASCII.GetBytes($chunk.Type))
            Write-BigEndianInt32 $writer (8 + $chunk.Bytes.Length)
            $writer.Write($chunk.Bytes)
        }
    } finally {
        $writer.Dispose()
        $stream.Dispose()
    }
}

Export-Sprite $sourceFiles.Player (Join-Path $assetsDirectory 'diver.png') 80 80 80 80 $false
Export-Sprite $sourceFiles.Fish (Join-Path $assetsDirectory 'enemy_small.png') 0 0 32 32 $true
Export-Sprite $sourceFiles.DartFish (Join-Path $assetsDirectory 'enemy_fast.png') 39 0 39 20 $true
Export-Sprite $sourceFiles.Fish (Join-Path $assetsDirectory 'enemy_piranha.png') 32 0 32 32 $true
Export-Sprite $sourceFiles.BigFish (Join-Path $assetsDirectory 'enemy_shark.png') 54 0 54 49 $true
New-GameBackground
New-Harpoon
New-OxygenTank

$landscapeMaster = New-KeyArt 1920 1080 $false $false
try {
    Save-Png $landscapeMaster (Join-Path $storeArtDirectory 'key-art-master-v1.png')
} finally {
    $landscapeMaster.Dispose()
}

$landscapeBranded = New-KeyArt 1920 1080 $false $true
try {
    Save-Png $landscapeBranded (Join-Path $storeArtDirectory 'key-art-branded-landscape-v1.png')
} finally {
    $landscapeBranded.Dispose()
}

$portraitBranded = New-KeyArt 1200 1800 $true $true
try {
    Save-Png $portraitBranded (Join-Path $storeArtDirectory 'key-art-branded-portrait-v1.png')
} finally {
    $portraitBranded.Dispose()
}

$wordmarkSource = New-Wordmark $false
try {
    Save-Png $wordmarkSource (Join-Path $storeArtDirectory 'title-wordmark-source-v1.png')
} finally {
    $wordmarkSource.Dispose()
}

$wordmark = New-Wordmark $true
try {
    Save-Png $wordmark (Join-Path $storeArtDirectory 'title-wordmark-v1.png')
} finally {
    $wordmark.Dispose()
}

$iconMaster = New-IconMaster
try {
    Export-PngIcon $iconMaster (Join-Path $iconsDirectory 'deepdive-icon-source.png') 1024
    Export-PngIcon $iconMaster (Join-Path $androidDrawableDirectory 'deepdive_icon.png') 512
    Export-PngIcon $iconMaster (Join-Path $playAssetsDirectory 'icon-512.png') 512
    Export-PngIcon $iconMaster (Join-Path $lwjglResourcesDirectory 'deepdive128.png') 128
    Export-PngIcon $iconMaster (Join-Path $lwjglResourcesDirectory 'deepdive64.png') 64
    Export-PngIcon $iconMaster (Join-Path $lwjglResourcesDirectory 'deepdive32.png') 32
    Export-PngIcon $iconMaster (Join-Path $lwjglResourcesDirectory 'deepdive16.png') 16
    Export-Ico $iconMaster (Join-Path $iconsDirectory 'logo.ico')
    Export-Icns $iconMaster (Join-Path $iconsDirectory 'logo.icns')
} finally {
    $iconMaster.Dispose()
    $fontCollection.Dispose()
}

& (Join-Path $PSScriptRoot 'export-store-art.ps1')

Write-Host 'CC0/OFL game, icon, and storefront artwork is ready.'

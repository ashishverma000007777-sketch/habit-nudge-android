#!/usr/bin/env python3
"""
Generate Habit Nudge Brand Assets, Adaptive Icons, Vector Drawables, and Multi-Density Mipmaps.
Visual Identity: Deep Charcoal (#101114), Dark Slate (#191B20), Primary Accent Orange (#FF8A3D),
Secondary Accent Sunrise Amber (#FFC078), Momentum Emerald (#35C98A), White (#F5F5F5).
"""

import os
import math
from PIL import Image, ImageDraw, ImageFilter, ImageFont

PROJECT_ROOT = "/data/data/com.termux/files/home/habit-nudge-android"
RES_DIR = os.path.join(PROJECT_ROOT, "app/src/main/res")
DOCS_ASSETS_DIR = os.path.join(PROJECT_ROOT, "docs/assets")

os.makedirs(DOCS_ASSETS_DIR, exist_ok=True)

# 1. Android Adaptive Icon Vector XMLs

# Background XML (Deep Charcoal with subtle elevated center glow)
BACKGROUND_XML = """<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <!-- Deep obsidian base background -->
    <path
        android:fillColor="#101114"
        android:pathData="M0,0h108v108h-108z" />
    
    <!-- Subtle concentric elevated plate -->
    <path
        android:fillColor="#181A1F"
        android:pathData="M54,54m-42,0a42,42 0 1,0 84,0a42,42 0 1,0 -84,0" />
        
    <!-- Inner ambient focal glow -->
    <path
        android:fillColor="#21242B"
        android:pathData="M54,54m-28,0a28,28 0 1,0 56,0a28,28 0 1,0 -56,0" />
</vector>
"""

# Foreground XML (108dp with all active graphics strictly inside the 72dp safe area centered at 54, 54)
# R=23dp. Center = 54, 54. Safe diameter = 66dp.
FOREGROUND_XML = """<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:aapt="http://schemas.android.com/aapt"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">

    <!-- Subtle Guide Habit Track Ring (Dark Slate) -->
    <path
        android:strokeColor="#2E333D"
        android:strokeWidth="3.5"
        android:strokeLineCap="round"
        android:fillColor="#00000000"
        android:pathData="M54,30 A24,24 0 1,0 78,54" />

    <!-- Active Momentum Arc: Sweeps dynamically from 180° around through 0° to 45° -->
    <path
        android:strokeWidth="4.2"
        android:strokeLineCap="round"
        android:fillColor="#00000000"
        android:pathData="M30,54 A24,24 0 1,1 70.97,70.97">
        <aapt:attr name="android:strokeColor">
            <gradient
                android:startX="30"
                android:startY="54"
                android:endX="74"
                android:endY="46"
                android:type="linear">
                <item android:offset="0.0" android:color="#FF8A3D" />
                <item android:offset="0.6" android:color="#FFA25A" />
                <item android:offset="1.0" android:color="#FFC078" />
            </gradient>
        </aapt:attr>
    </path>

    <!-- Momentum Progression Pip 1 (Small) -->
    <path
        android:fillColor="#FF8A3D"
        android:pathData="M73,39m-1.8,0a1.8,1.8 0 1,0 3.6,0a1.8,1.8 0 1,0 -3.6,0" />

    <!-- Momentum Progression Pip 2 (Medium - Nudge Spark) -->
    <path
        android:fillColor="#FFC078"
        android:pathData="M76.5,47m-2.2,0a2.2,2.2 0 1,0 4.4,0a2.2,2.2 0 1,0 -4.4,0" />

    <!-- Momentum Progression Pip 3 (Emerald Completion Spark) -->
    <path
        android:fillColor="#35C98A"
        android:pathData="M76,57m-2.6,0a2.6,2.6 0 1,0 5.2,0a2.6,2.6 0 1,0 -5.2,0" />

    <!-- Core Dynamic Nudge Arrow / Forward Energy Spark -->
    <!-- Stylized Chevron representing forward momentum -->
    <path
        android:strokeColor="#FFFFFF"
        android:strokeWidth="3.6"
        android:strokeLineCap="round"
        android:strokeLineJoin="round"
        android:fillColor="#00000000"
        android:pathData="M48,44 L58,54 L48,64" />

    <!-- Inner Momentum Core Point -->
    <path
        android:fillColor="#FF8A3D"
        android:pathData="M42,54m-2.5,0a2.5,2.5 0 1,0 5,0a2.5,2.5 0 1,0 -5,0" />
</vector>
"""

# Monochrome XML (For Android 13+ Material You themed icons)
MONOCHROME_XML = """<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <!-- Active Momentum Arc in Pure White for System Tinting -->
    <path
        android:strokeColor="#FFFFFF"
        android:strokeWidth="4.2"
        android:strokeLineCap="round"
        android:fillColor="#00000000"
        android:pathData="M30,54 A24,24 0 1,1 70.97,70.97" />

    <!-- Pips -->
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M73,39m-1.8,0a1.8,1.8 0 1,0 3.6,0a1.8,1.8 0 1,0 -3.6,0" />
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M76.5,47m-2.2,0a2.2,2.2 0 1,0 4.4,0a2.2,2.2 0 1,0 -4.4,0" />
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M76,57m-2.6,0a2.6,2.6 0 1,0 5.2,0a2.6,2.6 0 1,0 -5.2,0" />

    <!-- Core Forward Chevron -->
    <path
        android:strokeColor="#FFFFFF"
        android:strokeWidth="3.6"
        android:strokeLineCap="round"
        android:strokeLineJoin="round"
        android:fillColor="#00000000"
        android:pathData="M48,44 L58,54 L48,64" />

    <!-- Inner Core Dot -->
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M42,54m-2.5,0a2.5,2.5 0 1,0 5,0a2.5,2.5 0 1,0 -5,0" />
</vector>
"""

# Adaptive Icon Mipmap Definition
ADAPTIVE_ICON_XML = """<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
    <monochrome android:drawable="@drawable/ic_launcher_monochrome" />
</adaptive-icon>
"""

# Complete In-App Brand Logo Vector (for Splash, Settings, About)
BRAND_LOGO_XML = """<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:aapt="http://schemas.android.com/aapt"
    android:width="120dp"
    android:height="120dp"
    android:viewportWidth="120"
    android:viewportHeight="120">
    <!-- Elevated Dark Card Badge -->
    <path
        android:fillColor="#191B20"
        android:pathData="M20,6 C11.16,6 4,13.16 4,22 L4,98 C4,106.84 11.16,114 20,114 L96,114 C104.84,114 112,106.84 112,98 L112,22 C112,13.16 104.84,6 96,6 Z" />
    <path
        android:strokeColor="#2E333D"
        android:strokeWidth="1.5"
        android:fillColor="#00000000"
        android:pathData="M20,6 C11.16,6 4,13.16 4,22 L4,98 C4,106.84 11.16,114 20,114 L96,114 C104.84,114 112,106.84 112,98 L112,22 C112,13.16 104.84,6 96,6 Z" />

    <!-- Background Arc Track -->
    <path
        android:strokeColor="#282D37"
        android:strokeWidth="5"
        android:strokeLineCap="round"
        android:fillColor="#00000000"
        android:pathData="M60,26 A34,34 0 1,0 94,60" />

    <!-- Glowing Momentum Arc -->
    <path
        android:strokeWidth="5.5"
        android:strokeLineCap="round"
        android:fillColor="#00000000"
        android:pathData="M26,60 A34,34 0 1,1 84.04,84.04">
        <aapt:attr name="android:strokeColor">
            <gradient
                android:startX="26"
                android:startY="60"
                android:endX="90"
                android:endY="48"
                android:type="linear">
                <item android:offset="0.0" android:color="#FF8A3D" />
                <item android:offset="0.6" android:color="#FFA25A" />
                <item android:offset="1.0" android:color="#FFC078" />
            </gradient>
        </aapt:attr>
    </path>

    <!-- Habit Pips -->
    <path
        android:fillColor="#FF8A3D"
        android:pathData="M87,38m-2.5,0a2.5,2.5 0 1,0 5,0a2.5,2.5 0 1,0 -5,0" />
    <path
        android:fillColor="#FFC078"
        android:pathData="M92,50m-3,0a3,3 0 1,0 6,0a3,3 0 1,0 -6,0" />
    <path
        android:fillColor="#35C98A"
        android:pathData="M91,64m-3.5,0a3.5,3.5 0 1,0 7,0a3.5,3.5 0 1,0 -7,0" />

    <!-- Forward Kinetic Chevron -->
    <path
        android:strokeColor="#F5F5F5"
        android:strokeWidth="4.5"
        android:strokeLineCap="round"
        android:strokeLineJoin="round"
        android:fillColor="#00000000"
        android:pathData="M52,46 L66,60 L52,74" />

    <!-- Pulse Anchor Dot -->
    <path
        android:fillColor="#FF8A3D"
        android:pathData="M43,60m-3.5,0a3.5,3.5 0 1,0 7,0a3.5,3.5 0 1,0 -7,0" />
</vector>
"""

# Standalone Master SVG Logo (Brand Mark + Wordmark)
MASTER_LOGO_SVG = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 880 260" width="880" height="260">
  <defs>
    <linearGradient id="nudgeGrad" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#FF8A3D" />
      <stop offset="60%" stop-color="#FFA25A" />
      <stop offset="100%" stop-color="#FFC078" />
    </linearGradient>
    <linearGradient id="cardGrad" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#1D2026" />
      <stop offset="100%" stop-color="#141519" />
    </linearGradient>
    <filter id="subtleGlow" x="-20%" y="-20%" width="140%" height="140%">
      <feGaussianBlur stdDeviation="8" result="blur" />
      <feComposite in="SourceGraphic" in2="blur" operator="over" />
    </filter>
  </defs>

  <!-- Dark Canvas Background -->
  <rect width="880" height="260" rx="28" fill="#101114" />
  <rect x="1" y="1" width="878" height="258" rx="27" fill="none" stroke="#22252B" stroke-width="2" />

  <!-- App Icon Badge Group -->
  <g transform="translate(40, 30)">
    <!-- Squircle Elevated Base -->
    <rect width="200" height="200" rx="46" fill="url(#cardGrad)" stroke="#2B303B" stroke-width="2" />
    
    <!-- Ambient Inner Ring -->
    <circle cx="100" cy="100" r="62" fill="none" stroke="#22262F" stroke-width="8" stroke-linecap="round" />
    
    <!-- Dynamic Glowing Momentum Arc -->
    <path d="M 38 100 A 62 62 0 1 1 143.84 143.84" fill="none" stroke="url(#nudgeGrad)" stroke-width="9" stroke-linecap="round" />

    <!-- Progression Pips -->
    <circle cx="149" cy="60" r="4.5" fill="#FF8A3D" />
    <circle cx="158" cy="82" r="5.5" fill="#FFC078" />
    <circle cx="156" cy="107" r="6.5" fill="#35C98A" />

    <!-- Core Forward Chevron (The Nudge) -->
    <path d="M 85 75 L 110 100 L 85 125" fill="none" stroke="#F5F5F5" stroke-width="8" stroke-linecap="round" stroke-linejoin="round" />
    <circle cx="70" cy="100" r="6" fill="#FF8A3D" />
  </g>

  <!-- Typography Brand Mark -->
  <g transform="translate(280, 115)">
    <!-- Habit Nudge Title -->
    <text x="0" y="0" font-family="-apple-system, BlinkMacSystemFont, 'Inter', 'Segoe UI', Roboto, sans-serif" font-size="58" font-weight="700" letter-spacing="-1">
      <tspan fill="#F5F5F5">Habit</tspan>
      <tspan fill="#FF8A3D" dx="14">Nudge</tspan>
    </text>

    <!-- Tagline -->
    <text x="4" y="44" font-family="-apple-system, BlinkMacSystemFont, 'Inter', 'Segoe UI', Roboto, sans-serif" font-size="20" font-weight="500" fill="#A3A7B0" letter-spacing="2.5">
      SMALL HABITS. STRONGER YOU.
    </text>

    <!-- Micro Spec Badges -->
    <g transform="translate(4, 76)">
      <rect width="118" height="24" rx="6" fill="#1C1F26" />
      <text x="12" y="16" font-family="sans-serif" font-size="11" font-weight="600" fill="#FF8A3D" letter-spacing="1">PREMIUM M3</text>

      <rect x="128" width="132" height="24" rx="6" fill="#1C1F26" />
      <text x="138" y="16" font-family="sans-serif" font-size="11" font-weight="600" fill="#35C98A" letter-spacing="1">100% OFFLINE</text>

      <rect x="270" width="144" height="24" rx="6" fill="#1C1F26" />
      <text x="280" y="16" font-family="sans-serif" font-size="11" font-weight="600" fill="#FFC078" letter-spacing="1">SMART REMINDERS</text>
    </g>
  </g>
</svg>
"""

# Standalone Master SVG App Icon
MASTER_ICON_SVG = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" width="512" height="512">
  <defs>
    <linearGradient id="iconNudgeGrad" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#FF8A3D" />
      <stop offset="60%" stop-color="#FFA25A" />
      <stop offset="100%" stop-color="#FFC078" />
    </linearGradient>
    <linearGradient id="iconBgGrad" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#1A1C22" />
      <stop offset="100%" stop-color="#0F1013" />
    </linearGradient>
  </defs>

  <!-- Deep Obsidian Squircle Base -->
  <rect width="512" height="512" rx="118" fill="url(#iconBgGrad)" />
  <rect x="2" y="2" width="508" height="508" rx="116" fill="none" stroke="#262A34" stroke-width="4" />

  <!-- Base Momentum Track -->
  <circle cx="256" cy="256" r="148" fill="none" stroke="#222630" stroke-width="20" stroke-linecap="round" />

  <!-- Vibrant Momentum Progress Arc -->
  <path d="M 108 256 A 148 148 0 1 1 360.65 360.65" fill="none" stroke="url(#iconNudgeGrad)" stroke-width="24" stroke-linecap="round" />

  <!-- Compounding Progression Pips -->
  <circle cx="373" cy="160" r="11" fill="#FF8A3D" />
  <circle cx="395" cy="212" r="13.5" fill="#FFC078" />
  <circle cx="390" cy="272" r="16" fill="#35C98A" />

  <!-- Forward Kinetic Chevron (The Nudge) -->
  <path d="M 220 195 L 282 256 L 220 317" fill="none" stroke="#FFFFFF" stroke-width="20" stroke-linecap="round" stroke-linejoin="round" />
  
  <!-- Anchor Pulse Dot -->
  <circle cx="184" cy="256" r="15" fill="#FF8A3D" />
</svg>
"""


def render_high_res_icon(size=1024, is_round=False):
    """Render a pixel-perfect, supersampled high-resolution icon bitmap using PIL."""
    # Render at 2x scale for sharp anti-aliasing
    scale = 2
    canvas_size = size * scale
    img = Image.new("RGBA", (canvas_size, canvas_size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    cx, cy = canvas_size // 2, canvas_size // 2

    # Background
    if is_round:
        radius = canvas_size // 2 - (4 * scale)
        draw.ellipse(
            [cx - radius, cy - radius, cx + radius, cy + radius],
            fill=(20, 22, 27, 255),
            outline=(40, 44, 54, 255),
            width=4 * scale,
        )
    else:
        # Smooth Squircle / Rounded Rect
        corner_radius = int(0.23 * canvas_size)
        draw.rounded_rectangle(
            [4 * scale, 4 * scale, canvas_size - 4 * scale, canvas_size - 4 * scale],
            radius=corner_radius,
            fill=(20, 22, 27, 255),
            outline=(40, 44, 54, 255),
            width=4 * scale,
        )

    # Ambient subtle center disc
    inner_r = int(0.38 * canvas_size)
    draw.ellipse(
        [cx - inner_r, cy - inner_r, cx + inner_r, cy + inner_r],
        fill=(26, 29, 36, 255),
    )

    # Momentum Track ring (Dark slate)
    track_r = int(0.29 * canvas_size)
    track_width = int(0.046 * canvas_size)
    draw.arc(
        [cx - track_r, cy - track_r, cx + track_r, cy + track_r],
        start=0,
        end=360,
        fill=(36, 40, 50, 255),
        width=track_width,
    )

    # Momentum Progress Arc (Orange Flame: 180° to 45° clockwise)
    # PIL arc draws from start to end in degrees clockwise (0 is 3 o'clock)
    draw.arc(
        [cx - track_r, cy - track_r, cx + track_r, cy + track_r],
        start=180,
        end=405,  # 180 deg to 45 deg clockwise
        fill=(255, 138, 61, 255),
        width=track_width,
    )

    # Draw rounded cap at start (180 deg = left)
    start_x = cx - track_r
    start_y = cy
    cap_r = track_width // 2
    draw.ellipse(
        [start_x - cap_r, start_y - cap_r, start_x + cap_r, start_y + cap_r],
        fill=(255, 138, 61, 255),
    )

    # Draw rounded cap at end (45 deg = bottom-right)
    rad_end = math.radians(45)
    end_x = int(cx + track_r * math.cos(rad_end))
    end_y = int(cy + track_r * math.sin(rad_end))
    draw.ellipse(
        [end_x - cap_r, end_y - cap_r, end_x + cap_r, end_y + cap_r],
        fill=(255, 192, 120, 255),
    )

    # Progression Pips (Ascending trio)
    # Pip 1: at angle ~300 deg (top right)
    for angle_deg, pip_r_factor, color in [
        (315, 0.022, (255, 138, 61, 255)),   # Orange
        (338, 0.026, (255, 192, 120, 255)),  # Sunrise Amber
        (5,   0.032, (53, 201, 138, 255)),   # Momentum Green
    ]:
        p_rad = math.radians(angle_deg)
        dist = track_r + int(0.045 * canvas_size)
        px = int(cx + dist * math.cos(p_rad))
        py = int(cy + dist * math.sin(p_rad))
        pr = int(pip_r_factor * canvas_size)
        draw.ellipse([px - pr, py - pr, px + pr, py + pr], fill=color)

    # Center Forward Chevron (The Nudge)
    chevron_stroke = int(0.04 * canvas_size)
    ch_top = (cx - int(0.07 * canvas_size), cy - int(0.12 * canvas_size))
    ch_mid = (cx + int(0.05 * canvas_size), cy)
    ch_bot = (cx - int(0.07 * canvas_size), cy + int(0.12 * canvas_size))

    draw.line([ch_top, ch_mid], fill=(255, 255, 255, 255), width=chevron_stroke)
    draw.line([ch_mid, ch_bot], fill=(255, 255, 255, 255), width=chevron_stroke)

    # Round caps for chevron lines
    ch_cap = chevron_stroke // 2
    for pt in [ch_top, ch_mid, ch_bot]:
        draw.ellipse([pt[0] - ch_cap, pt[1] - ch_cap, pt[0] + ch_cap, pt[1] + ch_cap], fill=(255, 255, 255, 255))

    # Core anchor pulse dot
    anchor_x = cx - int(0.14 * canvas_size)
    anchor_y = cy
    anchor_r = int(0.028 * canvas_size)
    draw.ellipse(
        [anchor_x - anchor_r, anchor_y - anchor_r, anchor_x + anchor_r, anchor_y + anchor_r],
        fill=(255, 138, 61, 255),
    )

    # Downsample cleanly to target size
    return img.resize((size, size), Image.Resampling.LANCZOS)


def generate_all():
    print("Writing vector XML assets...")
    # Write drawable XMLs
    with open(os.path.join(RES_DIR, "drawable/ic_launcher_background.xml"), "w") as f:
        f.write(BACKGROUND_XML)
    with open(os.path.join(RES_DIR, "drawable/ic_launcher_foreground.xml"), "w") as f:
        f.write(FOREGROUND_XML)
    with open(os.path.join(RES_DIR, "drawable/ic_launcher_monochrome.xml"), "w") as f:
        f.write(MONOCHROME_XML)
    with open(os.path.join(RES_DIR, "drawable/ic_habit_nudge_logo.xml"), "w") as f:
        f.write(BRAND_LOGO_XML)

    # Write mipmap-anydpi-v26 XMLs
    anydpi_dir = os.path.join(RES_DIR, "mipmap-anydpi-v26")
    os.makedirs(anydpi_dir, exist_ok=True)
    with open(os.path.join(anydpi_dir, "ic_launcher.xml"), "w") as f:
        f.write(ADAPTIVE_ICON_XML)
    with open(os.path.join(anydpi_dir, "ic_launcher_round.xml"), "w") as f:
        f.write(ADAPTIVE_ICON_XML)

    # Write Master SVGs
    with open(os.path.join(DOCS_ASSETS_DIR, "habit_nudge_logo.svg"), "w") as f:
        f.write(MASTER_LOGO_SVG)
    with open(os.path.join(DOCS_ASSETS_DIR, "habit_nudge_icon.svg"), "w") as f:
        f.write(MASTER_ICON_SVG)

    # Density Mipmaps (PNG fallback for launchers and notifications)
    densities = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192,
    }

    for folder, px in densities.items():
        out_dir = os.path.join(RES_DIR, folder)
        os.makedirs(out_dir, exist_ok=True)
        print(f"Generating {folder} ({px}x{px})...")
        # Regular squircle icon
        img_square = render_high_res_icon(size=px, is_round=False)
        img_square.save(os.path.join(out_dir, "ic_launcher.png"), "PNG")
        # Round icon
        img_round = render_high_res_icon(size=px, is_round=True)
        img_round.save(os.path.join(out_dir, "ic_launcher_round.png"), "PNG")

    # High-Res 512x512 Play Store icon
    print("Generating Play Store 512x512 icon...")
    play_icon = render_high_res_icon(size=512, is_round=False)
    play_icon.save(os.path.join(DOCS_ASSETS_DIR, "playstore-icon.png"), "PNG")

    # Master 1024x1024 Brand Asset
    print("Generating Master 1024x1024 Brand Icon...")
    master_icon = render_high_res_icon(size=1024, is_round=False)
    master_icon.save(os.path.join(DOCS_ASSETS_DIR, "habit_nudge_logo_1024.png"), "PNG")

    # Copy to Brain Artifacts directory for user display
    artifact_dir = "/data/data/com.termux/files/home/.gemini/antigravity-cli/brain/a28ae8f3-1209-4ae0-b3c1-ae5d6d505193"
    if os.path.exists(artifact_dir):
        play_icon.save(os.path.join(artifact_dir, "habit_nudge_app_icon.png"), "PNG")
        master_icon.save(os.path.join(artifact_dir, "habit_nudge_master_logo.png"), "PNG")
        with open(os.path.join(artifact_dir, "habit_nudge_logo.svg"), "w") as f:
            f.write(MASTER_LOGO_SVG)

    print("Brand assets successfully generated!")


if __name__ == "__main__":
    generate_all()

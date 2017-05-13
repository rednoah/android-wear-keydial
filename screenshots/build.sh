#!/bin/sh

OUTPUT="$PWD/figures"


banner_layouts=(
    banner_1_keydial banner_2_swipekey banner_3_qwerty
)
letters=(
    a b c d e f g
)


for i in "${!banner_layouts[@]}"; do
    convert "${banner_layouts[$i]}.png" \
        -gravity Center -background black -extent 550x600 \
        -gravity NorthWest -fill white -pointsize 55  -annotate +30+30 "${letters[$i]})" \
        "$OUTPUT/${banner_layouts[$i]}_${letters[$i]}.png"
done

convert $OUTPUT/banner_* -background none -append "$OUTPUT/stripe_banner_layouts.png"

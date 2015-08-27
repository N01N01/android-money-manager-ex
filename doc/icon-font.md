# Icon Font

There is a possibility of adding a font, similar to FontAwesome, and use scalable icons from the font
instead of multiple resource files for images.

## Links

To use font for icons, see

http://blog.shamanland.com/2013/11/how-to-use-icon-fonts-in-android.html

https://github.com/shamanland/fonticon

and Fontastic.me service.

http://blog.shamanland.com/p/android-fonticon-library.html

# Font

The custom font is named mmex.

Icons used:
- icon-law, octicons; a

# Usage

## As Image

    FontIconDrawable.inflate(getResources(), R.xml.ic_quote)

## As Text

    TextView selectorText = (TextView) convertView.findViewById(R.id.selectorText);
    Typeface typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/mmex.ttf");
    selectorText.setTypeface(typeface);
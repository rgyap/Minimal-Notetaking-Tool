# Markdown Specification

This serves as an explanation of how my converter practically works together with outlining certain quirks. 

To make sure I covered enough, I am following the outline for the basic syntax of Markdown from the [Markownguide.org website](https://www.markdownguide.org/basic-syntax/).

## Headings

To create a heading, type a hash (`#`) followed by a space and then the name of the heading. For example,

```
# Heading 1 (<h1>)
## Heading 2 (<h2>) 
### Heading 3 (<h3>)
#### Heading 4 (<h4>)
##### Heading 5 (<h5>)
###### Heading 6 (<h6>)
```

Leaving empty lines before or after a header is recommended but not necessary.

One can also create a Heading 1 and Heading 2 in the by typing out the name of the header and then, in a new line, type three or more `=` (for Heading 1) or `-` (for Heading 2) characters.

```
Heading 1
===
Heading 2
---
```

Again, leaving empty lines before or after a header (or rather, in this case, after the line with `=` and `-` characters) is recommended but not necessary.

## Paragraphs

To separate a body of text into different paragraphs, simply add a blank line between them.

```
This is a paragraph.

This is another.
```

So, when rendered into HTML, on expects to see something like (putting inside a block quote for better visibility):

> This is a paragraph.
> 
> This is another.

We do *not* get

> This is a paragraph.This is another.


## Line Breaks

In order to create a line break, just continue typing in the immediately next line.

```
This is a line.
Followed by another,
That is not separated
By a paragraph break.
```

When rendered, we get the following (placing inside a block quote for better visibility):

> This is a line.
> Followed by another,
> That is not separated
> By a paragraph break
> Or forced into a single line.

Indeed, we do *not* get

> This is a line.Followed by another,That is not separatedBy a paragraph break.Or forced into a single line.

nor do we get

> This is a line.
>
> Followed by another,
>
> That is not separated
>
> By a paragraph break
>
> Or forced into a single line.


## Emphasis

Using asterisks (`*`) or underscores (`_`), one can put emphasis on text.

### Italics

To write *italics* as if by using the `<i>` HTML tag, surround the text with a single asterisk (`*`) on both sides.
```
Italicize text *like this*.
```
Then, to opt for the `<em>` tag instead, use underscores instead of asterisks.
```
The _text_ is italicized using <em>, but one can use CSS to change this behavior
```

### Bold Text

To make text be rendered in **boldface** as if through using the `<b>` tag, put two asterisks (`**`) in front and after the text.
```
**Boldface** text.
```
Or, to use the `<strong>` tag instead, use underscores instead of asterisks as well.
```
The text is __apparently strong__. Again, the behavior of <strong> can be changed through CSS.
```

### Bold and Italic Text

To make text both ***bold and italic*** like `<i><b>this</b></i>`, surround the text with three asterisks (`***`) on both sides.
```
Use this style for ***extra emphasis***!
```
Alternatively, by using underscores instead, bold and italic text can be achieved through tags like `<em><strong>this</strong></em>`.
```
The text has ___strong emphasis___
```

### Combination of Asterisks and Underscores

Indeed, we essentially have the following "conversions":
- `*` is to `<i>` (or `</i>` when closing)
- `**` is to `<b>` (or `</b>` when closing)
- `***` is to `<i><b>` (or `</b></i>` when closing)
- `_` is to `<em>` (or `</em>` when closing)
- `__` is to `<strong>` (or `</strong>` when closing)
- `___` is to `<em><strong>` (or `</strong></em>` when closing)

These six strings can be arranged in any order (as long as they are also properly closed on the other side) to achieve a desired effect.

## Block Quotations

To create a block quote, add a greater than symbol (`>`) at the start of the line.

```
> This quote is **very inspiring**.
>
> *Source:* I made it up!
```

When rendered with the right CSS, the rendered text can look like this:
> This quote is **very inspiring**.
>
> *Source:* I made it up!

Blank lines are not needed before and after a block quote. Spaces after the last greater than symbol (`>`) are also not needed. Even though they are not necessary, I would still recommend to always do so.

Nesting is also possible with block quotes. For example, typing

```
> Lorem ipsum __dolor__ sit amet, consectetur adipiscing elit. 
>>>>> Duis *efficitur* id magna **vitae** posuere.
>>>>> Etiam egestas ***libero*** vel urna auctor, vel _tincidunt_ libero sodales.
>> ___Praesent___ sit amet fermentum ex.
>
> Suspendisse aliquet maximus erat id rutrum.
>>> Proin lacinia eros
>>> [quis](https://www.example.com/) dignissim fringilla.
> Nunc mattis metus at vulputate mollis.
```

One gets

> Lorem ipsum __dolor__ sit amet, consectetur adipiscing elit. 
>>>>> Duis *efficitur* id magna **vitae** posuere.
>>>>> Etiam egestas ***libero*** vel urna auctor, vel _tincidunt_ libero sodales.
>> ___Praesent___ sit amet fermentum ex.
>
> Suspendisse aliquet maximus erat id rutrum.
>>> Proin lacinia eros
>>> [quis](https://www.example.com/) dignissim fringilla.
> Nunc mattis metus at vulputate mollis.

Headers and lists are **not allowed** inside a block quote and will not be rendered properly.

## Lists

### Unordered Lists

To create an unordered list (a bullet-point list), start the line with a dash. For example,
```
- ***Top*** item
- **Middle** item
- *Second-to-the-last* item
- Final item
```
When rendered, it will look something like this:

- ***Top*** item
- **Middle** item
- *Second-to-the-last* item
- Final item

It is also *necessary* to place a blank line directly before and after an unordered list. Different levels in lists are also supported. To increase the level of a list by one, preceed the dash with *two spaces* (the converter is strict with the indentation). Lists levels can *only* be increased by 1. For example, the following list would render nicely
```
- A
  - B
    - C
      - D
    - E
- F
```

- A
  - B
    - C
      - D
    - E
- F

But the following **will not** render as expected because *invalid HTML* will be generated.
```
- A
      - B
    - C
  - D
- E

- A
    - B
        -C
```

### Ordered Lists

To create an ordered list, start the line with `1.`, a one followed by a period. Other items can then be added to the list by starting the next line starting with a number followed by a period. The level of a list can be increased by adding *two spaces* at the front, but only an increase of one is allowed. Also, make sure to have one blank line before and after a list. For example,

```
1. One
2. Two
  1. Twoo
    1. Twooo 
3. Three
  1. Threee
  2. Threeee
4. Four
```

will appear as

1. One
2. Two
  1. Twoo
    1. Twooo 
3. Three
  1. Threee
  2. Threeee
4. Four

In fact, those numbers used to type out the list actually *do not matter* at all, provided that the *very first item* of the list starts with `1.`. For example, typing

```
1. One
2000. Two
  21. Twoo
    2101. Twooo 
3. Three
  31. Threee
  332. Threeee
5. Four
```

would result in the same ordered list shown previously.

### Adding Text Below an Item of a List

To add content below an item of a list (whether ordered or unordered), add a line directly below said list item, type anything within that single line, then add a number of spaces at the start of the new line equal to two more than the number of spaces that preceeds the list item. For example, the lists
```
- Lorem ipsum
  Dolor sit amet
  - Consectetur 
    Adipiscing elit.
  - Sed
    - Do eiusmod tempor incididunt
      Ut labore
    - Et dolore
      Magna aliqua
  - Ut enim ad minim veniam
```
would appear as

- Lorem ipsum
  Dolor sit amet
  - Consectetur 
    Adipiscing elit.
  - Sed
    - Do eiusmod tempor incididunt
      Ut labore
    - Et dolore
      Magna aliqua
  - Ut enim ad minim veniam

This also works for ordered lists.


Text to be added below a list **cannot** be split into different lines or paragraphs. To do so, please use `<p>` and `<br>` tags instead of making new lines. For example,

1. First Case
  <p>Lorem ipsum dolor sit amet consectetur adipiscing elit.</p><p>Vivamus sollicitudin id purus non bibendum.</p>
2. Second Case
  Curabitur fermentum sodales purus, eu tincidunt diam dictum quis.<br>Proin facilisis nisl non purus congue pellentesque at id metus.



### Separating Two Lists of the Same Type

Two lists of the same type (ordered or unordered list) *must* be separated by *two* empty lines. For example, notice how the following
```
1. One
3. Two
3. Three

1. Four
2. Five
3. Six
```
does not really appear as it was written but instead is rendered as
1. One
3. Two
3. Three

1. Four
2. Five
3. Six

However, if instead we had
```
1. One
3. Two
3. Three


1. Four
2. Five
3. Six
```

It will appear as intended:

1. One
3. Two
3. Three


1. Four
2. Five
3. Six


### Combining Unordered and Ordered Lists

Doing so will not be supported. So, it's either one writes a fully unordered list or a fully ordered list, no mixing.

## Code

To write text in a source-code style within a paragraph, one can put a backtick (`&#96;`) on both sides of the text. For example,
```
I think having to write `public static void main(String[] args)` is ***painful***, especially if I just want to write a *Hello World* program.
```
would be rendered as (putting inside a block quote for better visibility)

> I think having to write `public static void main(String[] args)` is ***painful***, especially if I just want to write a *Hello World* program.

To write an entire block of source code while preserving its appearance, one can, in a new line, type three backticks (`&#96;&#96;&#96;`), place code in the lines after that, and then finally close it up with three backticks in the line after the final line of code. For example, the text
```
&#96;&#96;&#96;
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello World!");
    }
}
&#96;&#96;&#96;
```
would be rendered as

```
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello World!");
    }
}
```

Indeed, I have been using code blocks from the start!

Note that typing `&#96;&#96;&#96;` inside a code block is not allowed. To display a backtick inside code text, one must use the HTML entity `&amp;#96;`.

## Horizontal Rules

To create a horizontal rule along a line, put three or more dashes (`-`) or underscores (`_`), and be careful to not put any other character on the line. Also, make sure to keep blank lines directly above or below the line. For example,
```
Text

---

More text
```

Without space above the line, the text above might be turned into a header. Such a problem is nonexistent when using underscores to create a horizontal rule, but adding space is still good practice.

## Links

### Markdown-style Links
Indeed, one can create a link in Markdown as follows:
```
[display text for the link](url-of-link.com)
```
However, if any right square bracket (`]`) were to appear in the display text, it *must* be preceeded by a backslash. Moreover, any right parenthesis (`)`) in the URL *must* replaced with `%29`, and all underscores (`_`) within the URL must be preceeded by a backslash. For example,
```
[[VERY SAD!\] sad violin](https://www.youtube.com/watch?v=QuNhTLVgV2Y)

[Effects of brainwashing on members of the "Monolith" quasireligion](https://stalker.fandom.com/wiki/Monolith#Effects\_of\_mind\_control)
```

When rendered, one gets

> [[VERY SAD!\] sad violin](https://www.youtube.com/watch?v=QuNhTLVgV2Y)
>
> [Effects of brainwashing on members of the "Monolith" quasireligion](https://stalker.fandom.com/wiki/Monolith#Effects\_of\_mind\_control)

Without `https://` in the link, a browser might assume that the thing being linked to is a local file located in the same directory as the HTML file that is displaying said link.

Adding titles for links is *not supported*.

So-called "[reference-style links](https://www.markdownguide.org/basic-syntax/#reference-style-links)" will also not be supported. 

### Wiki-style Links

I also implemented Wiki-style links to allow notes to link to other Markdown notes within the notes directory.

```
[[name_of_note.md|display text]]
```

As such, the file `name_of_note` is expected to have `.md` as its file extension. The converter will then generate an HTML link containing a *relative path* to `name_of_note.html` (notice the change from `.md` to `.html`). This works because the file structure of the converted notes is identical to that of the original directory containing all the Markdown files in addition to how the converter preserves the original file name but the extension.

Right square brackets and underscores *do not need* to be preceeded by a backslash when inside a Wiki-style link address. However, all right square brackets in the display text *must* be preceeded by a backslash.

## Images

### Markdown-style Images

The format for an image is as follows:

```
![Alt text for the image](link to the image)
```

Just like with Markdown-style links, all right square brackets (`]`) inside the alt text must be preceeded by a backslash.

Additionally, all underscores (`_`) must be preceeded by a backslash, and closing parentheses (`)`) must be replaced with `%29`.

### Wiki-style Images

Wiki-style images are also implemented in order to accomodate exclusively for locally stored images inside the *folder containing the Markdown files*.

```
![[nice_image.png|image_size]]
```
The path to the image present in the `src` attribute of the corresponding `img` tag will contain a *relative path* to the target image, which the program shall clone into the folder containing all the converted files.

Just like with Wiki-style links, all right square brackets (`]`) and underscores (`_`) do not need to be preceeded by a backslash in the file name.

The `image_size` is any number that represents the desired width of the image in pixels (while maintaining the original proportions of the image). Accordingly, my program expects a number. But, if a percent is included, it will instead be sized relative to the width of the `div` that contains it. Indeed, other units allowed in HTML/CSS are allowed to be placed inside the `image_size` text.


### Images in Links

Following the rules mentioned above, it is possible for an image to act as a link. For example, one can do the following:

```
[![sobbing\](https://images.emojiterra.com/twitter/512px/1f62d.png)](https://www.youtube.com/watch?v=QuNhTLVgV2Y) 

[![[oh\_god\_why.jpg|50\]\]](https://en.wikipedia.org/wiki/Sadness#Coping\_mechanisms)  

[[TEST2.html|![[oh_god_why.jpg|100\]\]]]
```


## Escaping Characters

If a backslash precedes a character, the program would ensure that it remains in the output unmodified. For example, typing `**\*this\***` would appear as **\*this\*** (boldface `*this*`) and not ***this*** (boldface and italic `this`).

## HTML

HTML is fully supported as this program essentially just copies text from a Markdown document and outputs it into an HTML document.

## MathJax

The use of MathJax to display equations typed in $\LaTeX$ is fully supported. So, beautiful math equations like $e^{\pi i} + 1 = 0$ can be included in these notes and will be rendered nicely. To create an in-line expression, put a dollar sign `$` at the start and end of the expression. For example,
```
The golden ratio is explicitly given by $\varphi = \frac{1+\sqrt{5}}{2}$.
```
> The golden ratio is explicitly given by $\varphi = \frac{1+\sqrt{5}}{2}$.

Then, to create a math expression for display, use double dollar signs `$$` instead of single ones. For example

```
It is therefore evident that
$$1 = \begin{cases}
2 &\text{if $2=1$,}\\
1 &\text{if you're not an idiot.}
\end{cases}$$
```

Gives (using a block quote for better visibility):

> It is therefore evident that
> $$1 = \begin{cases} 2 &\text{if $2=1$,}\\1 &\text{if you're not an idiot.}\end{cases}$$

In fact, to use a display math within a block quote, just type the equation within a single line.

```
> It is therefore evident that
> $$1 = \begin{cases} 2 &\text{if $2=1$,}\\1 &\text{if you're not an idiot.}\end{cases}$$
```

However, note that an escaped dollar symbol `\$` cannot be used inside any quation. If you really want to have a dollar symbol in a MathJax expression, please type `\char"0024` instead. For example,

```
Hence, $\char"0024 1 + \char"0024 2 = \char"0024 3$.
```

will display the equation `$1 + $2 = $3`:

> Hence, $\char"0024 1 + \char"0024 2 = \char"0024 3$.


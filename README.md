# Minimal-Notetaking-Tool
CLI tools for making notes written in Markdown.

# Under Construction!
Nothing to show yet!

# Ok, but what is this supposed to be?
This is essentially like a minimalist version of apps like [Obsidian](https://obsidian.md/). The only interface this requires is the Command Line Interface (CLI).

The target basic workflow for now is as follows:
- Create or edit Markdown note files in a specific directory using any text editor (for example, I am using Vim).
- When satisfied with the content, use this tool to turn the Markdown files into HTML pages.
- A local webpage would then be generated, containing links to the notes.

For the generated webpage, I hope to achieve the following:
- The links to the notes shall be organized in an outline with respect to the hierarchy of the files in the main directory of all the notes.
- The generated webpage would hopefully be modular. In particular, the webpage shall only contain the following types of pages:
    - a main navigation page
    - navigation page for each subdirectory of the main directory
    - page template for notes entries

I wish to have said types to be fully customizable. By making them modular, I hope that the customization process would be relatively painless.

# What do you mean by "minimal"?
I wish to create the smallest codebase I can to support all the functionality I want. As I had put functionality as the prime priority, I also wish not to create a UI for this (that's bloat imo); instead, the user must rely on the CLI to use this.

# So, is this basically a glorified Markdown to HTML converter?
**Yes.** No bloat - just writing notes and looking at pretty text!

# Why Java?
Because that is the language I'm most comfortable with. And I suppose it'd be nice for me to write this only once, even at the cost of potentially needing to debug everywhere. However, I might actually attempt to rewrite this in C someday.

# This seems ambitious
I'm just bored and curious.

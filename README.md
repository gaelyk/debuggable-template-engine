# Debuggable Template Engine

This library provides implementation of Groovy `TemplateEngine` with better error reporting
which maps the parse exception to the original template.

The template engine is fork or original `SimpleTemplateEngine`.

## Example

For following template with missing brackets after `if (true)` statment:

```
Hello world!
<% if (true) %>
This will fail due missing opening bracket!
<% } %>
```

You get following error message:


```

unexpected token: ;

1   : Hello world!
2   : <% if (true) %>
=   :                ^
3   : This will fail due missing opening bracket!
4   : <% } %>

The template was parsed into following script:

1   : out.print("""Hello world!
2   : """); if (true) ;
=   :                 ^
3   : out.print("""
4   : This will fail due missing opening bracket!
5   : """); } ;
6   : out.print("""""");
7   : 
8   : /* Generated by SimpleTemplateEngine */

```

" Vim syntax file
" Language: Lox
" Maintainer: Nicolay Caspersen Roness
"
" Usage:
"   Put in $VIMRUNTIME/syntax/lox.vim
"   Add command:
"     autocmd BufRead,BufNewFile *.lox set filetype=lox

if exists("b:current_syntax")
    finish
endif

" Keywords
syntax keyword loxKeywords var print if else while for

" String literals
syntax region loxString start=/\v"/ skip=/\v\\./ end=/\v"/

highlight default link loxKeywords  Keyword
highlight default link loxString    String

let b:current_syntax = "lox"

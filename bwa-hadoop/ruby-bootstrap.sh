#!/bin/bash

curl -L https://get.rvm.io | bash -s stable --autolibs=enable --ignore-dotfiles || exit 1
PATH=$PATH:~/.rvm/bin
source ~/.rvm/scripts/rvm
rvm install 2.1.2
gem install bundler

rvm use 2.1.2

## install gems if necessary


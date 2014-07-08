

git clone --branch 0.7.9a --depth 1  https://github.com/lh3/bwa.git
cd bwa
make

if [ $? -ne 0 ]; then
  echo "Failed to make bwa"
  exit $?
fi

ln -s `pwd`/bwa ~/bin/bwa

#PATH=$PATH:~/bwa
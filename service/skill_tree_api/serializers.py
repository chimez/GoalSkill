from rest_framework import serializers

from .models import (Level, Method, MethodFamily, Skill, SkillFamily, User,
                     UserMethod, UserSkill)


class LevelSerializer(serializers.ModelSerializer):
    class Meta:
        model = Level
        fields = ('id', 'short_name', 'name', 'describe', 'need_exp')


class MethodFamilySerializer(serializers.ModelSerializer):
    class Meta:
        model = MethodFamily
        fields = ('id', 'name')


class MethodSerializer(serializers.ModelSerializer):
    class Meta:
        model = Method
        fields = '__all__'


class SkillFamilySerializer(serializers.ModelSerializer):
    class Meta:
        model = SkillFamily
        fields = ('id', 'name')


class UserSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ('id', 'name', 'skill_family')


class SkillSerializer(serializers.ModelSerializer):
    class Meta:
        model = Skill
        fields = ('id', 'name', 'tier', 'skill_family', 'method_family')


class UserSkillSerializer(serializers.ModelSerializer):
    class Meta:
        model = UserSkill
        fields = ('id', 'user', 'skill', 'skill_exp', 'skill_level')


class UserMethodSerializer(serializers.ModelSerializer):
    class Meta:
        model = UserMethod
        fields = ('id', 'user', 'method', 'skill', 'method_times')


# 以下是为某些查询特设的序列器
class SkillNameSerializer(serializers.ModelSerializer):
    class Meta:
        model = Skill
        fields = ('id', 'name', 'tier')


class LevelShortNameSerializer(serializers.ModelSerializer):
    class Meta:
        model = Level
        fields = ('short_name', 'need_exp')


class AllSkillSerializer(serializers.Serializer):
    skill = SkillNameSerializer()
    skill_exp = serializers.IntegerField()
    skill_level = LevelShortNameSerializer()

    class Meta:
        model = UserSkill


class SkillMethodSerializer(serializers.ModelSerializer):
    method = MethodSerializer()
    method_times = serializers.IntegerField()

    class Meta:
        model = UserMethod
        fields = '__all__'
